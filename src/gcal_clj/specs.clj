(ns gcal-clj.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [common.config :as cfg]
            [common.specs]
            [common.specs.http]
            [gcal-clj.specs-macros :as sm]
            [gcal-clj.specs-event]
            [gcal-clj.specs-calendar]
            [gcal-clj.specs-error-responses]))



(sm/mapdef2 :s.gcal.http.response.events.list/body
  ; :req-un
  {:s.gcal.prop.events-list/kind #{"calendar#events"}
   :s.prop.gcal/etag             :s.http/etag}
  ; :opt-un
  {:s.prop.gcal.http.events.list/summary string?
   :s.prop.gcal/description              string?
   :s.prop.gcal/updated                  :s.prop/rfc-3339-timestamp
   :s.prop.gcal/timeZone                 string?
   :s.prop.gcal/accessRole               string?
   :s.prop.gcal/nextPageToken            string?
   :s.prop.gcal/nextSyncToken            string?
   :s.prop.gcal/defaultReminders         (s/coll-of :s.gcal.ent/reminder)
   :s.prop.gcal.event-list/items         (s/coll-of :s.gcal.ent/event)})



(s/def :s.gcal.http.response/events-list--success
  (s/keys :req-un [:s.http.response.200/status
                   :s.gcal.http.response.events.list/body]))

(s/def :s.gcal.http.response/events-list
  (or :s.gcal.http.response/events-list--success
      :s.gcal.http.response/error)) ; branching error spec



(defn logging-assert
  "If config allows – will check a spec and log exception if there's one.
   Doesn't throw, can be used on production."
  [m spec-name]
  (if cfg/gcal--check-specs-and-log?
    (let [explain (s/explain-data spec-name m)
          ex (if explain (ex-info "Spec assertion failed" explain))]
      (if ex (log/error ex))))
  m)
