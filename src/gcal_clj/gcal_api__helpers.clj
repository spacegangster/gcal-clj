(ns gcal-clj.gcal-api--helpers
  (:require [clojure.tools.logging :as log]))


(defn- api-resp-400->err-msg [api-resp]
  (-> api-resp :body :error :message))


(defn goog-resp->reason [resp]
  (let [reason (some-> resp :body :error :errors first :reason)]
    (case reason
      "rateLimitExceeded" :goog.reason/rate-limit-exceeded
      :goog.reason/bad-request)))

(defn handle-resp--soft [handle-id api-resp]
  ; (def ar2 api-resp)
  (case (:status api-resp)
    (200 201 202 203 204 206)
    (:body api-resp)

    400
    {:goog/status   :error
     :goog/api-resp api-resp
     :goog/reason   (goog-resp->reason api-resp)}

    (401 403)
    {:goog/status   :error
     :goog/api-resp api-resp
     :goog/reason   (goog-resp->reason api-resp)}


    404
    {:goog/status   :error
     :goog/api-resp api-resp
     :goog/reason   :goog.reason/not-found}

    410
    {:goog/status   :error
     :goog/api-resp api-resp
     :goog/reason   :goog.reason/gone}

    (500 501 503 504 510 502)
    {:goog/status   :error
     :goog/reason   :goog.reason/internal-or-network-error
     :goog/api-resp api-resp}

    (do (log/warn "-handling a response, unknown status" (pr-str api-resp))
        {:goog/api-resp api-resp})))


(defn handle-resp
  "Throws on 400s and 500s"
  [handle-id api-resp]
  ; (def ar2 api-resp)
  (case (:status api-resp)
    (200 201 202 203 204 206)
    (:body api-resp)

    400
    (let [api-err-msg (api-resp-400->err-msg api-resp)
          ex-info-msg (str handle-id ". Our request is bad: " api-err-msg)]
      (throw (ex-info ex-info-msg
                      {:goog/api-resp api-resp
                       :goog/reason   :goog.reason/bad-request})))

    (401 403)
    (throw (ex-info (str handle-id ". No access to the resource")
                    {:goog/api-resp api-resp
                     :goog/reason   :goog.reason/no-access}))


    404
    (throw (ex-info (str handle-id ". Not found")
                    {:goog/api-resp api-resp
                     :goog/reason   :goog.reason/not-found}))

    410
    (throw (ex-info (str handle-id ". Gone")
                    {:goog/api-resp api-resp
                     :goog/reason   :goog.reason/gone}))

    (500 501 503 504 510 502)
    (throw (ex-info (str handle-id ". No access to Google?") {:goog/api-resp api-resp}))

    (do (log/warn "-handling a response, unknown status" (pr-str api-resp))
        (throw (ex-info (str handle-id ". Don't know what to do")
                        {:goog/api-resp api-resp})))))
