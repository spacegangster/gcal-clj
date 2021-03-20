(ns gcal-clj.gcal-api
  "Local members return direct response bodies or throw stuff, see -handle-resp.
   Note the difference between a CalendarListEntry and a Calendar.
   https://developers.google.com/calendar/concepts/events-calendars#calendar_and_calendar_list
   First one has colours and :accessRole."
  (:require [clojure.set]
            [clj-http.client :as http]
            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]
            [ring.util.codec :as codec]
            [common.core :as cc]
            [common.functions :as f]
            [common.request-functions :as rf]
            [common.time.clj-helpers :as dt]
            [gcal-clj.gcal-api--helpers :as h]
            [gcal-clj.specs :as gcal.specs])
  (:import (java.util Map)
           (java.time Instant Duration LocalDateTime)
           (clojure.lang Keyword)))



(defn lc-keys [api-res]
  (f/map-keys #(cond-> % (string? %) (.toLowerCase)) api-res))

(def gcals-url-base
  "https://www.googleapis.com/calendar/v3/calendars")

(def kw->method
  {:http/get    http/get
   :http/post   http/post
   :http/put    http/put
   :http/patch  http/patch
   :http/delete http/delete
   :http/move   http/move})


(defn exec-api
  [{:goog/keys [access-token uri query-params ^Keyword method form-params] :as prms}]
  (log/info "exec-api, prms" (pr-str prms))
  (let [method (get kw->method method http/get)
        api-res (method uri
                  (cond->
                    {:headers {"Authorization" (str "Bearer " access-token)}
                     :throw-exceptions false
                     :query-params query-params}
                    form-params (assoc :form-params form-params
                                       :content-type :json)))
        api-res (update api-res :headers lc-keys)
        json-resp? (and (not (empty? (:body api-res)))
                        (= :c-types/json (rf/req->content-type-id api-res)))]
    (cond-> api-res
            json-resp?
            (update :body cc/json-str->edn))))


(defn exec-api-get
  [{:goog/keys [access-token uri query-params] :as prms}]
  (log/info "exec-api-get, prms" (pr-str prms))
  (let [api-res (http/get uri {:headers {"Authorization" (str "Bearer " access-token)}
                               :throw-exceptions false
                               :query-params query-params})
        api-res (update api-res :headers lc-keys)
        json-resp? (and (not (empty? (:body api-res)))
                        (= :c-types/json (rf/req->content-type-id api-res)))]
    (cond-> api-res
            json-resp?
            (update :body cc/json-str->edn))))


(comment
  (exec-api-get
    {:goog/access-token "long-gcal-token",
     :goog/uri          "https://www.googleapis.com/calendar/v3/users/me/calendarList"
     :scope             "https://www.googleapis.com/auth/calendar openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/calendar.events https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.settings.readonly",}))


;; calendars and settings

(defn fetch-settings [{:goog/keys [access-token] :as tokens}]
  ; https://developers.google.com/calendar/v3/reference/settings
  (let [api-resp
        (exec-api-get {:goog/access-token access-token,
                       :goog/uri "https://www.googleapis.com/calendar/v3/users/me/settings"})]
    ; (def set1 (h/handle-resp--t "GET Settings" api-resp))
    (h/handle-resp "GET Settings" api-resp)))

(defn calendar-get [tokens calendar-id]
  (h/handle-resp
    (str "calendar-get " calendar-id)
    (exec-api-get
      {:goog/access-token (:goog/access-token tokens)
       :goog/uri          (str gcals-url-base "/" (codec/url-encode calendar-id))})))

(defn calendar-get--list-entry [tokens calendar-id]
  (let [uri (str "https://www.googleapis.com/calendar/v3/users/me/calendarList/" (codec/url-encode calendar-id))]
    (h/handle-resp
      (str "calendar-get--list-entry " calendar-id)
      (exec-api-get
        {:goog/access-token (:goog/access-token tokens)
         :goog/uri          uri}))))


(defn calendar-insert [tokens {:keys [summary] :as calendar}]
  (h/handle-resp--soft
    (str "calendar-insert " summary)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/method       :http/post
       :goog/form-params  calendar
       :goog/uri          gcals-url-base})))

(defn calendar-patch [tokens calendar-id calendar-data]
  ; https://developers.google.com/calendar/v3/reference/calendars/patch
  (h/handle-resp
    (str "calendar-patch " calendar-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/method       :http/patch
       :goog/form-params  calendar-data
       :goog/uri          (str gcals-url-base "/" (codec/url-encode calendar-id))})))

(defn calendar-delete [tokens calendar-id]
  ; https://developers.google.com/calendar/v3/reference/calendars/delete
  (h/handle-resp
    (str "calendar-delete " calendar-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/method       :http/delete
       :goog/uri          (str gcals-url-base "/" (codec/url-encode calendar-id))})))

(defn calendar-watch
  "id  string  A UUID or similar unique string that identifies this channel.
   token  string  An arbitrary string delivered to the target address with each notification delivered over this channel. Optional.
   type  string  The type of delivery mechanism used for this channel.
     web_hook
   address  string  The address where notifications are delivered for this channel.
   params  object  Additional parameters controlling delivery channel behavior. Optional.
   params.ttl"
  [tokens calendar-id prms]
  (exec-api
    {:goog/access-token (:goog/access-token tokens)
     :goog/method       :http/post
     :goog/form-params  prms
     :goog/uri          (str gcals-url-base "/" (codec/url-encode calendar-id) "/events/watch")}))


(defn calendar-get-all [{:goog/keys [access-token] :as tokens}]
  (let [api-resp
        (exec-api-get {:goog/access-token access-token,
                       :goog/uri "https://www.googleapis.com/calendar/v3/users/me/calendarList"})]
    ; (def ar-call (h/handle-resp--t "calendar-list-all " api-resp))
    (h/handle-resp "calendar-list-all " api-resp)))

(comment
  (spit "gcal-embassy/resources/gcal-resp-calendars.edn"
        (with-out-str (pprint/pprint ar-set1))))

(comment
  (calendar-get-all
    {:goog/access-token "long-gcal-token"}))

(comment
  ; see calendars sample for data sample
  gcal-clj.dataset-one/calendars-sample
  gcal-clj.dataset-one/calendar-list-response)



;; events ;;


(defn event-get [tokens calendar-id event-id]
  (h/handle-resp--soft
    (str "event-get " calendar-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/method       :http/get
       :goog/uri          (str gcals-url-base
                               "/" (codec/url-encode calendar-id)
                               "/events/" (codec/url-encode event-id))})))

(comment
  (event-get
    {:goog/access-token "long-gcal-token"}
    "hello@example.io"
    ""))


(defn event-insert [tokens calendar-id event]
  (let [calendarId (codec/url-encode calendar-id)]
    (h/handle-resp
      (str "event-insert " calendar-id)
      (exec-api
        {:goog/access-token (:goog/access-token tokens)
         :goog/method       :http/post
         :goog/form-params  event
         :goog/uri          (str gcals-url-base "/" calendarId "/events")}))))


(defn- auto-crutch-event
  "Compensate for
  https://stackoverflow.com/questions/35655324/changing-event-start-date-from-date-to-datetime"
  [{:keys [start end] :as event}]
  (cond-> event
          start (assoc :start (merge {:date nil} start))
          end (assoc :end (merge {:date nil} end))))

(defn event-patch
  "Sends patch request to patch event.
  Applies a little crutch to :start :end coordinates
  @link https://developers.google.com/calendar/v3/reference/events/patch"
  [tokens calendar-id event-id event]
; (def epa1 [tokens calendar-id event-id event])
  (h/handle-resp
    (str "event-patch cal-id #" calendar-id " evt-id #" event-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/method       :http/patch
       :goog/form-params  (auto-crutch-event event)
       :goog/uri          (str gcals-url-base "/"
                               (codec/url-encode calendar-id)
                               "/events/" (codec/url-encode event-id))})))

(comment
  (apply event-patch epa1))


(defn event-move [tokens {:keys [event-id src-cal-id dst-cal-id] :as opts}]
; (def em1 [tokens opts])
  (h/handle-resp
    (str "event-move src-cal-id #" src-cal-id " dst-cal-id #" dst-cal-id " evt-id #" event-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/query-params {:destination dst-cal-id}
       :goog/method       :http/post
       :goog/uri          (str gcals-url-base "/"
                               (codec/url-encode src-cal-id)
                               "/events/" (codec/url-encode event-id)
                               "/move")})))

(comment
  (apply event-move em1))


(defn event-delete
  "https://developers.google.com/calendar/v3/reference/events/delete
   ^map :params :
     {^boolean :sendUpdates}"
  [tokens calendar-id event-id & [{:keys [sendUpdates] :as opts}]]
  ; (def em1 [tokens opts])
  (h/handle-resp
    (str "event-delete src-cal-id #" calendar-id " evt-id #" event-id)
    (exec-api
      {:goog/access-token (:goog/access-token tokens)
       :goog/query-params {:destination calendar-id}
       :goog/method       :http/delete
       :goog/uri          (str gcals-url-base "/"
                               (codec/url-encode calendar-id)
                               "/events/"
                               (codec/url-encode event-id))})))


(comment
  (let [cal-evt-ids backend.data-access-layer.nodes/to-delete
        len (count cal-evt-ids)
        atom:ctr (atom 0)]
    (for [[cal-id evt-id] cal-evt-ids]
      (try
        (prn (swap! atom:ctr inc) "/" len)
        (event-delete tokens* cal-id evt-id)
        (catch Exception e
          (log/error e))))))


(defn- events-list--internal
  "Could use :syncToken here"
  [^String access-token ^String cal-id ^Map query-params]
  ;https://developers.google.com/calendar/v3/reference/events/list?hl=en
  (log/info "fetching cal events" cal-id query-params)
  (let [cal-id-uri (codec/url-encode cal-id)
        uri (str gcals-url-base "/" cal-id-uri "/events")
        res (exec-api-get
              {:goog/access-token access-token
               :goog/uri          uri
               :goog/query-params query-params})]
    (def eli-r1 res)
    (log/info "fetch succeeded, fetched" (count (:items res)) "items")
    (gcal.specs/logging-assert res :s.gcal.http.response/events-list)
    res))

(comment
  (-> eli-r1 :body :items (get 447))
  (keys eli-r1)

  (let [items (-> eli-r1 :body :items)
        len (count items)]
    (dotimes [i len]
      (let [item (nth items i)]
        (try
          (if (s/explain-data :s.gcal.ent/event item)
            (prn ::issue-with i))
          (catch Exception e
            (prn ::issue-with i))))))

  (gcal.specs/logging-assert
    (-> eli-r1 :body :items)
    :s.prop.gcal.event-list/items)
  (gcal.specs/logging-assert eli-r1 :s.gcal.http.response/events-list))


(defn- enrich-w-recurring-bases
  "To ensure that we download all recurring events bases"
  [g-events access-token gcal-id]
  (def a1 [g-events access-token gcal-id])
  (let [grouped (group-by :recurringEventId g-events)
        rec-derived (dissoc grouped nil)
        required-base-ids (f/keyset rec-derived)
        base-and-regs (get grouped nil)]
    (if (empty? required-base-ids)
      {:items g-events}
      (let [rec-bases (f/index-by-id (filterv (comp required-base-ids :id) base-and-regs))
            rec-base-ids (f/keyset rec-bases)
            ids-to-load (clojure.set/difference required-base-ids rec-base-ids)]
        (if (empty? ids-to-load)
          {:items g-events}
          (let [-eg (partial event-get {:goog/access-token access-token} gcal-id)
                base-evts-resps (pmap -eg ids-to-load)
                -grouped (group-by :goog/reason base-evts-resps)
                base-evts (get -grouped nil)
                failed-reasons (not-empty (dissoc -grouped nil))]

            (when failed-reasons
              (future
                (log/error (str "Failed to download some recurring event bases: \n"
                                (pr-str failed-reasons)))))

            (cond-> {:items (vec (concat base-evts g-events))}
              failed-reasons (assoc :gcal/failures failed-reasons))))))))

(comment
  (keys)
  (apply enrich-w-recurring-bases a1))



(defn events-list--raw-for-polling
  "Only to be used in polling/sync-tag. Returns http response
   :body will contain gcal of shape :s.ent/gcal
   Events packed in items"
  [^String access-token, ^String gcal-id, ^String next-sync-token]
  (def el1 [access-token gcal-id next-sync-token])
  (let [http-resp (events-list--internal access-token gcal-id {:syncToken next-sync-token})
        _ (def hr1 http-resp)
        items-raw (get-in http-resp [:body :items])
        _ (def ir1 items-raw)
        w-recur-resp (enrich-w-recurring-bases items-raw access-token gcal-id)]
    (-> (update http-resp :body f/assign w-recur-resp)
        (gcal-clj.specs/logging-assert :s.gcal.http.response/events-list))))

(comment
  hr1
  (require '[clojure.spec.alpha :as s])
  (s/explain :s.gcal.http.response/events-list hr1)
  (apply events-list--raw-for-polling el1))


(defn events-list
  "For successful responses shape is :s.ent/gcal"
  [access-token gcal-id & [opts]]
  ; Must be an RFC3339 timestamp with mandatory time zone offset, for example,
  ; 2011-06-03T10:00:00-07:00, 2011-06-03T10:00:00Z
  (let [time-boundaries {:timeMin (.toString (.plus (Instant/now) (Duration/ofDays -730))) ; two years back
                         :timeMax (.toString (.plus (Instant/now) (Duration/ofDays 730)))} ; both boundaries are exclusive
        max-res 2000
        ; goog doesn't fetch to the limit !!
        opts-0 (merge time-boundaries {:maxResults max-res} opts) ; use 2000
        res-0 (h/handle-resp
                (str "events-list cal-id #" gcal-id)
                (events-list--internal access-token gcal-id opts-0))]
    (loop [prev-opts opts-0
           prev-resp res-0
           iter 2]
      #_(log/info "\n\n\n loop conditions " (= max-res (count (:items prev-resp))) (< iter 6) (:nextPageToken prev-resp))
      (if-let [npt (and (< iter 6) (:nextPageToken prev-resp))]
        (let [opts (assoc prev-opts :pageToken npt)
              prev-items (:items prev-resp)
              _ (log/info (str "going for a fetch #" iter))
              resp (update (h/handle-resp
                             (str "events-list cal-id #" gcal-id)
                             (events-list--internal access-token gcal-id opts))
                           :items #(concat prev-items %))]
          (recur opts resp (inc iter)))
        (let [w-recurring (enrich-w-recurring-bases (:items prev-resp) access-token gcal-id)
              failures (:gcal/failures w-recurring)]
          (when (nil? (:items w-recurring))
            (def wrec1 w-recurring)
            (def pr1 (:items prev-resp)))
          (cond-> (assoc prev-resp :items (:items w-recurring))
                  failures (update :gcal/failures #(merge-with into % failures))
                  1 (gcal.specs/logging-assert :s.gcal.http.response.events.list/body)))))))

(comment
  (type wrec1))


(defn- inline-events [^String access-token ^Map calendar]
  (let [cal-id (:id calendar)
        events-resp (events-list access-token cal-id)
        events-items (:items events-resp)
        nst (:nextSyncToken events-resp)]
  ; (def er1 events-resp)
    (cond-> (assoc calendar
              :events events-items
              :goog.events/last-poll-ts (dt/now-timestamp))
            nst (assoc :goog.events/next-sync-token nst))))



(defn enrich-calendars-w-events [tokens calendars]
  (let [at (:goog/access-token tokens)
        resp (mapv #(inline-events at %) calendars)]
  ; (def rich-cals resp)
    resp))



(comment
  (def at1 "long-gcal-token")
  (calendar-insert {:goog/access-token at1} {:summary "New cal"})

  (def e2 (events-list at1 "hello@example.io"
                       {:timeMin (str (.. (LocalDateTime/of 2020 11 9 0 0 0 0) toString) ":00.0Z")
                        :timeMax (str (.. (LocalDateTime/of 2020 11 13 20 0) toString) ":00.0Z")}))

  (enrich-w-recurring-bases
    (subvec (:items e2) 1)
    at1 "hello@example.io")


  (filterv :recurringEventId e2)
  (keys e2)

  (def el3 (events-list--raw-for-polling at1 "hello@example.io" ""))
  (-> el3 :body (dissoc :items))
  (-> el3 :body keys)


  (def e1
    (events-list at1 "hello@example.io"))

  (enrich-calendars-w-events {:goog/access-token at1}))

