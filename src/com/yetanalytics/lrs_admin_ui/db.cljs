(ns com.yetanalytics.lrs-admin-ui.db
  (:require [cljs.spec.alpha  :as s :include-macros true]
            [re-frame.core    :as re-frame]
            [xapi-schema.spec :as xs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec to define the db
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def :session/page keyword?)
(s/def :session/token (s/nilable string?))
(s/def :session/username (s/nilable string?))
(s/def ::session
  (s/keys :req-un [:session/page
                   :session/token
                   :session/username]))

(s/def :login/username (s/nilable string?))
(s/def :login/password (s/nilable string?))
(s/def ::login
  (s/keys :req-un [:login/username
                   :login/password]))


(s/def :credential/api-key string?)
(s/def :credential/secret-key string?)

(s/def :credential/scope string?)
(s/def :credential/scopes (s/every :credential/scope))

(s/def ::credential
  (s/keys :req-un [:credential/api-key
                   :credential/secret-key
                   :credential/scopes]))

(s/def ::credentials
  (s/every ::credential))

(s/def :new-account/username (s/nilable string?))
(s/def :new-account/password (s/nilable string?))

(s/def ::new-account
  (s/keys :req-un [:new-account/username
                   :new-account/password]))

(s/def :account/username (s/nilable string?))
(s/def :account/account-id string?)

(s/def ::account
  (s/keys :req-un [:account/account-id
                   :account/username]))

(s/def ::accounts (s/every ::account))

(s/def :browser/content (s/nilable string?))
(s/def :browser/address (s/nilable string?))
(s/def :browser/credential (s/nilable
                            (s/keys :req-un [:credential/api-key
                                             :credential/secret-key
                                             :credential/scopes])))

(s/def ::browser
  (s/keys :req-un [:browser/content
                   :browser/address
                   :browser/credential]))

(s/def ::server-host string?)
(s/def ::xapi-prefix string?) ;; default /xapi

(s/def :notification/id int?)
(s/def :notification/error? boolean?)
(s/def :notification/msg (s/nilable string?))

(s/def ::notification (s/keys :req-un [:notification/error?
                                       :notification/msg]))

(s/def ::notifications (s/every ::notification))

(s/def ::enable-statement-html boolean?)

(s/def ::oidc-auth boolean?)
(s/def ::oidc-enable-local-admin boolean?)

(s/def ::enable-admin-status boolean?)

(s/def :status.data/statement-count nat-int?)
(s/def :status.data/actor-count nat-int?)
(s/def :status.data/last-statement-stored (s/nilable ::xs/timestamp))
;; The JSON->edn conversion makes the platform a keyword, so we handle that in
;; the sub.
(s/def :status.data/platform-frequency (s/map-of keyword? nat-int?))

(s/def :status.data.timeline/stored ::xs/timestamp)
(s/def :status.data.timeline/count nat-int?)

(s/def :status.data/timeline
  (s/every
   (s/keys :req-un [:status.data.timeline/stored
                    :status.data.timeline/count])))

(s/def :status/data
  (s/keys :opt-un [:status.data/statement-count
                   :status.data/actor-count
                   :status.data/last-statement-stored
                   :status.data/platform-frequency
                   :status.data/timeline]))

(s/def :status.params/timeline-unit
  #{"year"
    "month"
    "day"
    "hour"
    "minute"
    "second"})
(s/def :status.params/timeline-since
  ::xs/timestamp)
(s/def :status.params/timeline-until
  ::xs/timestamp)

(s/def :status/params
  (s/keys :opt-un [:status.params/timeline-unit
                   :status.params/timeline-since
                   :status.params/timeline-until]))

;; map of vis type to loading state
(s/def :status/loading
  (s/map-of #{"statement-count"
              "actor-count"
              "last-statement-stored"
              "platform-frequency"
              "timeline"}
            boolean?))

(s/def ::status
  (s/keys
   :opt-un [:status/data
            :status/params
            :status/loading]))

(s/def ::db (s/keys :req [::session
                          ::credentials
                          ::login
                          ::browser
                          ::accounts
                          ::server-host
                          ::xapi-prefix
                          ::notifications
                          ::enable-statement-html
                          ::oidc-auth
                          ::oidc-enable-local-admin
                          ::enable-admin-status
                          ::status]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Continuous DB Validation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn check-and-throw
  "Throw an exception if the app db does not match the spec."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "Spec check failed in: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor
  (re-frame/after
   (partial check-and-throw ::db)))
