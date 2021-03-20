(ns gcal-clj.specs-error-responses
  "Common Google Calendar error responses.
   Original doc https://developers.google.com/calendar/v3/errors"
  (:require [clojure.spec.alpha :as s]
            [common.specs.http]))


;;; spec for a general error from a response body ;;;
(s/def :s.gcal.http.errors/item
  (s/keys :req-un [::domain ::reason ::message]
          :opt-un [::location ::locationType]))

(s/def :s.gcal.http.error/code (s/int-in 400 600))
(s/def :s.gcal.http.error/message string?)
(s/def :s.gcal.http.error/errors (s/coll-of :s.gcal.http.errors/item))

(s/def :s.gcal.http/error
  (s/keys :req-un
          [:s.gcal.http.error/code
           :s.gcal.http.error/message
           :s.gcal.http.error/errors]))

; the http body
(s/def :s.gcal.http.response.error/body
  (s/keys :req-un [:s.gcal.http/error]))

(assert
  (s/valid? :s.gcal.http.response.error/body
    {:error
     {:errors
               [{:domain       "calendar",
                 :reason       "timeRangeEmpty",
                 :message      "The specified time range is empty.",
                 :locationType "parameter",
                 :location     "timeMax"}],
      :code    400,
      :message "The specified time range is empty."}}))



(s/def :s.gcal.http.response.error/calendar-bad-request
  (s/keys :req-un [:s.http.error.request/status
                   :s.gcal.http.response.error/body]))

{:error
 {:errors
           [{:domain       "calendar",
             :reason       "timeRangeEmpty",
             :message      "The specified time range is empty.",
             :locationType "parameter",
             :location     "timeMax"}],
  :code    400,
  :message "The specified time range is empty."}}


(s/def :s.gcal.http.response.error/invalid-credentials
  (s/keys :req-un [:s.http.error.unauthorized/status
                   :s.gcal.http.response.error/body]))

{:error
 {:errors
           [{:domain       "global",
             :reason       "authError",
             :message      "Invalid Credentials",
             :locationType "header",
             :location     "Authorization"}],
  :code    401,
  :message "Invalid Credentials"}}


; 403

(s/def :s.gcal.http.response.error.limit-exceeded/daily
  (s/keys :req-un [:s.http.error.forbidden/status
                   :s.gcal.http.response.error/body]))

{:error
 {:errors [{:domain  "usageLimits",
            :reason  "dailyLimitExceeded",
            :message "Daily Limit Exceeded"}],
  :code    403,
  :message "Daily Limit Exceeded"}}

(s/def :s.gcal.http.response.error.limit-exceeded/user
  (s/keys :req-un [:s.http.error.forbidden/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors  [{:domain  "usageLimits",
             :reason  "userRateLimitExceeded",
             :message "User Rate Limit Exceeded"}],
  :code    403,
  :message "User Rate Limit Exceeded"}}

(s/def :s.gcal.http.response.error/limit-exceeded
  (s/keys :req-un [:s.http.error.forbidden/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors
           [{:domain  "usageLimits",
             :reason  "rateLimitExceeded",
             :message "Rate Limit Exceeded"}],
  :code    403,
  :message "Rate Limit Exceeded"}}

(s/def :s.gcal.http.response.error/calendar-usage-limits-exceeded
  (s/keys :req-un [:s.http.error.forbidden/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors
           [{:domain "usageLimits",
             :message "Calendar usage limits exceeded.",
             :reason "quotaExceeded"}],
  :code 403,
  :message "Calendar usage limits exceeded."}}


(s/def :s.gcal.http.response.error/forbidden-for-non-organizer
  (s/keys :req-un [:s.http.error.forbidden/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors
           [{:domain  "calendar",
             :reason  "forbiddenForNonOrganizer",
             :message "Shared properties can only be changed by the organizer of the event."}],
  :code    403,
  :message "Shared properties can only be changed by the organizer of the event."}}


; 404
; The specified resource was not found.
; - when the requested resource (with the provided ID) has never existed
; - when accessing a calendar that the user can not access
; - something else
(s/def :s.gcal.http.response.error/not-found
  (s/keys :req-un [:s.http.error.not-found/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors [{:domain  "global",
            :reason  "notFound",
            :message "Not Found"}],
  :code    404,
  :message "Not Found"}}


; 409
(s/def :s.gcal.http.response.error/conflict
  (s/keys :req-un [:s.http.error.conflict/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors [{:domain  "global",
            :reason  "duplicate",
            :message "The requested identifier already exists."}],
  :code    409,
  :message "The requested identifier already exists."}}


; 410
(s/def :s.gcal.http.response.error.gone/next-sync-token
  (s/keys :req-un [:s.http.error.gone/status
                   :s.gcal.http.response.error/body]))

{:error
 {:errors
           [{:domain       "calendar",
             :reason       "fullSyncRequired",
             :message      "Sync token is no longer valid, a full sync is required.",
             :locationType "parameter",
             :location     "syncToken"}],
  :code 410,
  :message "Sync token is no longer valid, a full sync is required."}}

(s/def :s.gcal.http.response.error.gone/invalid-updated-min
  (s/keys :req-un [:s.http.error.gone/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors
           [{:domain       "calendar",
             :reason       "updatedMinTooLongAgo",
             :message      "The requested minimum modification time lies too far in the past.",
             :locationType "parameter",
             :location     "updatedMin"}],
  :code    410,
  :message "The requested minimum modification time lies too far in the past."}}

(s/def :s.gcal.http.response.error.gone/already-deleted
  (s/keys :req-un [:s.http.error.gone/status
                   :s.gcal.http.response.error/body]))
{:error
 {:errors  [{:domain  "global",
             :reason  "deleted",
             :message "Resource has been deleted"}],
  :code    410,
  :message "Resource has been deleted"}}


; 412
(s/def :s.gcal.http.response.error/precondition-failed
  (s/keys :req-un [:s.http.error.precondition/status
                   :s.gcal.http.response.error/body]))
"The etag supplied in the If-match header no longer corresponds to the current etag of the resource."
{:error
 {:errors [{:domain       "global",
            :reason       "conditionNotMet",
            :message      "Precondition Failed",
            :locationType "header",
            :location     "If-Match"}],
  :code    412,
  :message "Precondition Failed"}}


; 500
(s/def :s.gcal.http.response.error/backend-error
  (s/keys :req-un [:s.http.error.server-any/status]
          :opt-un [:s.gcal.http.response.error/body]))
"An unexpected error occurred while processing the request."
{:error
 {:errors [{:domain  "global",
            :reason  "backendError",
            :message "Backend Error"}],
  :code    500,
  :message "Backend Error"}}


(s/def :s.gcal.http.response/error
  (s/or  :s.gcal.error/bad-request                :s.gcal.http.response.error/calendar-bad-request
         :s.gcal.error/invalid-credentials        :s.gcal.http.response.error/invalid-credentials
         :s.gcal.error.limit/daily                :s.gcal.http.response.error.limit-exceeded/daily
         :s.gcal.error.limit/user                 :s.gcal.http.response.error.limit-exceeded/user
         :s.gcal.error/limit                      :s.gcal.http.response.error/limit-exceeded
         :s.gcal.error/forbidden                  :s.gcal.http.response.error/forbidden-for-non-organizer
         :s.gcal.error/not-found                  :s.gcal.http.response.error/not-found
         :s.gcal.error.gone/sync-token            :s.gcal.http.response.error.gone/next-sync-token
         :s.gcal.error.gone/invalid-updated-min   :s.gcal.http.response.error.gone/invalid-updated-min
         :s.gcal.error.gone/already-deleted       :s.gcal.http.response.error.gone/already-deleted
         :s.gcal.error/precondition               :s.gcal.http.response.error/precondition-failed
         :s.gcal.error/bad-request                :s.gcal.http.response.error/backend-error))


(assert
  (s/valid?
    :s.gcal.http.response/error
    {:status 500
     :body   {:error
              {:errors  [{:domain  "global",
                          :reason  "backendError",
                          :message "Backend Error"}],
               :code    500,
               :message "Backend Error"}}}))

(assert
  (s/valid?
    :s.gcal.http.response/error
    {:status 400
     :body   {:error
              {:errors  [{:domain  "global",
                          :reason  "backendError",
                          :message "Backend Error"}],
               :code    400,
               :message "Backend Error"}}}))

(comment
  (s/conform
    :s.gcal.http.response/error
    {:status 400
     :body   {:error
              {:errors  [{:domain  "global",
                          :reason  "backendError",
                          :message "Backend Error"}],
               :code    400,
               :message "Backend Error"}}}))
