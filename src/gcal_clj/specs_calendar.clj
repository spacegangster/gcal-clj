(ns gcal-clj.specs-calendar
  "Google's doc
  https://developers.google.com/calendar/v3/reference/calendars
  https://developers.google.com/calendar/v3/reference/calendarList"
  (:require [clojure.spec.alpha :as s]
            [common.specs]
            [common.specs.http]
            [gcal-clj.specs-event]
            [gcal-clj.specs-macros :as sm]))

(s/def :s.gcal.prop.calendar/kind #{"calendar#calendar"})

(sm/mapdef1 :s.gcal.prop/notification-settings
  {:s.prop.gcal/notifications (s/coll-of :s.gcal.ent/reminder)})

(sm/mapdef1 :s.gcal.prop/conference-properties
  {:s.prop.gcal/allowedConferenceSolutionTypes (s/coll-of string?)})


; Calendar List entry
; https://developers.google.com/calendar/v3/reference/calendarList#resource
(sm/def-map-props
  {:s.gcal.prop.calendar-list-entry/kind #{"calendar#calendarListEntry"},
   :s.prop.gcal/id                       :s.prop/id_goog})

(sm/def-map-props
  ; :opt-un for calendar list entry
  {:s.prop.gcal/description          string?
   :s.prop.gcal/location             string?
   :s.prop.gcal/timeZone             string?
   :s.prop.gcal/summaryOverride      string?
   :s.prop.gcal/colorId              string?
   :s.prop.gcal/backgroundColor      string?
   :s.prop.gcal/foregroundColor      string?
   :s.prop.gcal/hidden               boolean?
   :s.prop.gcal/selected             boolean?
   :s.prop.gcal/accessRole           string?
   :s.prop.gcal/defaultReminders     (s/coll-of :s.gcal.ent/reminder)
   :s.prop.gcal/notificationSettings :s.gcal.prop/notification-settings
   :s.prop.gcal/primary              boolean?,
   :s.prop.gcal/deleted              boolean?,
   :s.prop.gcal/conferenceProperties :s.gcal.prop/conference-properties})


(s/def :s.gcal.ent/calendar-list-entry
  (s/keys :req-un
          [:s.gcal.prop.calendar-list-entry/kind
           :s.http/etag
           :s.prop.gcal/id
           :s.prop.gcal/summary]

          :opt-un
          [:s.prop.gcal/description
           :s.prop.gcal/location
           :s.prop.gcal/timeZone
           :s.prop.gcal/summaryOverride
           :s.prop.gcal/colorId
           :s.prop.gcal/backgroundColor
           :s.prop.gcal/foregroundColor
           :s.prop.gcal/hidden
           :s.prop.gcal/selected
           :s.prop.gcal/accessRole
           :s.prop.gcal/defaultReminders
           :s.prop.gcal/notificationSettings
           :s.prop.gcal/primary
           :s.prop.gcal/deleted
           :s.prop.gcal/conferenceProperties]))



; Calendar plain https://developers.google.com/calendar/v3/reference/calendars#resource
(s/def :s.gcal.ent/calendar
  (s/keys
    :req-un [:s.gcal.prop.calendar/kind
             :s.http/etag
             :s.prop.gcal/summary]
    :opt-un [:s.prop.gcal/description
             :s.prop.gcal/timeZone
             :s.prop.gcal/nextSyncToken
             :s.prop.gcal/updated
             :s.prop.gcal/accessRole
             :s.prop.gcal/defaultReminders
             :s.prop.gcal/items]))
