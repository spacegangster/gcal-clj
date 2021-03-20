(ns gcal-clj.specs-event
  "Google's doc
  https://developers.google.com/calendar/v3/reference/events#resource"
  (:require [clojure.spec.alpha :as s]
            [common.specs]
            [common.specs.http]
            [gcal-clj.specs-macros :as sm]))


(s/def :s.prop-type.goog/time-coord
  (s/keys :req-un [(or :s.prop.goog/dateTime :s.prop.goog/date)]
          :opt-un [:s.prop.goog/timeZone]))

(s/def :s.prop.goog/dateTime string?)
(s/def :s.prop.goog/date string?)
(s/def :s.prop.goog/timeZone string?)
(s/def :s.prop/rfc-3339-timestamp string?)
(s/def :s.http/etag string?)
(s/def :s.prop.gcal/id string?)
(s/def :s.prop-type/uri string?)
(s/def :s.prop-type/not-empty-string string?)

(s/def :s.prop.gcal/summary string?) ; can be empty or non-present
(s/def :s.prop.gcal/created :s.prop/rfc-3339-timestamp)
(s/def :s.prop.gcal/updated :s.prop/rfc-3339-timestamp)

(s/def :s.prop-type.gcal/prop-map
  (s/map-of (s/or :s string? :k keyword?) string?))
(s/valid? :s.prop-type.gcal/prop-map {:ff "33", "fa" "fa"})
(sm/mapdef1 :s.gcal.ent/reminder
  {:s.prop.gcal.reminder/method  string?,
   :s.prop.gcal.reminder/minutes nat-int?})

(s/def :s.prop.gcal/responseStatus
  #{"needsAction"  ; - The attendee has not responded to the invitation.
    "declined"     ; - The attendee has declined the invitation.
    "tentative"    ; - The attendee has tentatively accepted the invitation.
    "accepted"})   ; - The attendee has accepted the invitation.



(s/def :s.prop.gcal/transparency
  ; Whether the event blocks time on the calendar. Optional. Possible values are:
  #{"opaque"        ; - Default value. The event does block time on the calendar. This is equivalent to setting Show me as to Busy in the Calendar UI.
    "transparent"}) ; - The event does not block time on the calendar. This is equivalent to setting Show me as to Available in the Calendar UI.


(s/def :s.prop.gcal/visibility
  ; Visibility of the event. Optional. Possible values are:
  #{"default"        ; Uses the default visibility for events on the calendar. This is the default value.
    "public"         ; The event is public and event details are visible to all readers of the calendar.
    "private"        ; The event is private and only event attendees may view event details.
    "confidential"}) ; The event is private. This value is provided for compatibility reasons.

(s/def :s.prop.gcal/eventType
  ; Specific type of the event. Read-only. Possible values are:
  #{"default"       ; A regular event or not further specified.
    "outOfOffice"}) ; An out-of-office event.

(s/def :s.prop.gcal/status
  #{"confirmed"   ; The event is confirmed. This is the default status.
    "tentative"   ; The event is tentatively confirmed.
    "cancelled"}) ; The event is cancelled (deleted).
; The list method returns cancelled events only on incremental sync
; (when syncToken or updatedMin are specified) or if the showDeleted flag
; is set to true. The get method always returns them.

(s/def :s.prop.gcal.evt-cancelled/status #{"cancelled"})




;;;;; Person ;;;;;

(sm/def-map-props
  {:s.prop.gcal.person/id        string? ; google plus id
   :s.prop.gcal/displayName      string?
   :s.prop.gcal.person/organizer boolean?
   :s.prop.gcal/self             boolean? ; is organiser?
   :s.prop.gcal/resource         boolean?
   :s.prop.gcal/optional         boolean?
   :s.prop.gcal/comment          string?
   :s.prop.gcal/additionalGuests pos-int?})

(s/def :s.gcal.ent/person
  ; used for creator, organizer, and attendee fields
  (s/keys :opt-un
          [:s.prop.gcal/id
           :s.prop/email
           :s.prop.gcal/displayName
           :s.prop.gcal/self]))

(s/def :s.gcal.ent/attendee
  (s/merge :s.gcal.ent/person
    (s/keys
      :opt-un
      [:s.prop.gcal.person/organizer
       :s.prop.gcal/resource
       :s.prop.gcal/optional
       :s.prop.gcal/responseStatus
       :s.prop.gcal/comment
       :s.prop.gcal/additionalGuests])))


(sm/mapdef-opt :s.gcal.prop/ext-props
  {:s.prop.gcal/private :s.prop-type.gcal/prop-map
   :s.prop.gcal/shared  :s.prop-type.gcal/prop-map})


(sm/mapdef1 :s.gcal.prop/create-request-conf-sol-key
  {:s.prop.gcal/type string?})

(sm/mapdef1 :s.prop.gcal/create-request-status
  {:s.prop.gcal/statusCode string?})

(sm/mapdef1 :s.gcal.ent/create-request
  {:s.prop.gcal/requestId             string?
   :s.prop.gcal/conferenceSolutionKey :s.gcal.prop/create-request-conf-sol-key
   :s.prop.gcal.create-request/status :s.prop.gcal/create-request-status})


(sm/def-map-props
  {:s.prop.gcal.evt.entry-point/uri            :s.prop-type/uri
   :s.prop.gcal.evt.entry-point/label          :s.prop-type/not-empty-string
   :s.prop.gcal.evt.entry-point/pin            string?,
   :s.prop.gcal.evt.entry-point/accessCode     string?,
   :s.prop.gcal.evt.entry-point/meetingCode    string?,
   :s.prop.gcal.evt.entry-point/passcode       string?,
   :s.prop.gcal.evt.entry-point/password       string?})

(s/def :s.prop.gcal.evt.entry-point/entryPointType
  #{"video"  ; joining a conference over HTTP. A conference can have zero or one video entry point.
    "phone"  ; joining a conference by dialing a phone number. A conference can have zero or more phone entry points.
    "sip"    ; joining a conference over SIP. A conference can have zero or one sip entry point.
    "more"}) ; further conference joining instructions, for example additional phone numbers. A conference can have zero or one more entry point. A conference with only a more entry point is not a valid conference.

(s/def :s.gcal.prop/entry-point
  (s/keys :req-un [:s.prop.gcal.evt.entry-point/entryPointType]
          :opt-un [:s.prop.gcal.evt.entry-point/uri
                   :s.prop.gcal.evt.entry-point/label
                   :s.prop.gcal.evt.entry-point/pin
                   :s.prop.gcal.evt.entry-point/accessCode
                   :s.prop.gcal.evt.entry-point/meetingCode
                   :s.prop.gcal.evt.entry-point/passcode
                   :s.prop.gcal.evt.entry-point/password]))



(sm/mapdef1 :s.prop.gcal.evt/conference-solution
  {:s.prop.gcal/key     :s.prop.gcal/conferenceSolutionKey
   :s.prop.gcal/name    string?
   :s.prop.gcal/iconUri string?})


(sm/def-map-props
  {:s.prop.gcal.conf-data/createRequest      :s.gcal.ent/create-request
   :s.prop.gcal.conf-data/entryPoints        (s/coll-of :s.gcal.prop/entry-point)
   :s.prop.gcal.conf-data/conferenceSolution :s.prop.gcal.evt/conference-solution
   :s.prop.gcal.conf-data/conferenceId       string?
   :s.prop.gcal.conf-data/signature          string?
   :s.prop.gcal.conf-data/notes              string?})

(s/def :s.gcal.ent/conf-data
  (s/keys :req-un [(or :s.prop.gcal.conf-data/createRequest
                       :s.prop.gcal.conf-data/entryPoints)]
          :opt-un [:s.prop.gcal.conf-data/conferenceSolution
                   :s.prop.gcal.conf-data/conferenceId
                   :s.prop.gcal.conf-data/signature
                   :s.prop.gcal.conf-data/notes]))


; A gadget that extends this event.
; Gadgets are deprecated; this structure is instead only used for returning birthday calendar metadata.
(sm/mapdef2 :s.prop.gcal.evt/gadget
  {}
  {:s.prop.gcal/type        string?
   :s.prop.gcal/title       string?
   :s.prop.gcal/link        string?
   :s.prop.gcal/iconLink    string?
   :s.prop.gcal/width       nat-int?
   :s.prop.gcal/height      nat-int?
   :s.prop.gcal/display     string?
   :s.prop.gcal/preferences (s/map-of string? string?)})


(sm/mapdef2 :s.prop.gcal.evt/reminders
  {:s.prop.gcal/useDefault boolean?}
  {:s.prop.gcal/overrides  (s/coll-of :s.gcal.ent/reminder)})

(sm/mapdef1 :s.prop.gcal.evt/source
  ; Source from which the event was created. For example, a web page, an email message
  ; or any document identifiable by an URL with HTTP or HTTPS scheme.
  ; Can only be seen or modified by the creator of the event.
  {:s.prop.gcal/url   string?
   :s.prop.gcal/title string?})

(sm/mapdef1 :s.ent.gcal/attachment
  {:s.prop.gcal/fileUrl  string?
   :s.prop.gcal/title    string?
   :s.prop.gcal/mimeType string?
   :s.prop.gcal/iconLink string?
   :s.prop.gcal/fileId   string?})


(sm/def-map-props
  {:s.gcal.prop.event/kind #{"calendar#event"}
   :s.prop.gcal/htmlLink   string?

   :s.prop.gcal/description             string?
   :s.prop.gcal/location                string?
   :s.prop.gcal/colorId                 string?
   :s.prop.gcal/creator                 :s.gcal.ent/person
   :s.prop.gcal/organizer               :s.gcal.ent/person
   :s.prop.gcal/start                   :s.prop-type.goog/time-coord
   :s.prop.gcal/end                     :s.prop-type.goog/time-coord
   :s.prop.gcal/endTimeUnspecified      boolean?

   :s.prop.gcal/recurrence              :s.prop/recurrence
   :s.prop.gcal/recurringEventId        :s.prop/id_goog
   :s.prop.gcal/originalStartTime       :s.prop-type.goog/time-coord

   :s.prop.gcal/iCalUID                 string?
   :s.prop.gcal/sequence                nat-int?
   :s.prop.gcal/attendees               (s/coll-of :s.gcal.ent/attendee)
   :s.prop.gcal/attendeesOmitted        boolean?
   :s.prop.gcal/extendedProperties      :s.gcal.prop/ext-props
   :s.prop.gcal/hangoutLink             :s.prop-type/url ; read-only
   :s.prop.gcal/conferenceData          :s.gcal.ent/conf-data
   :s.prop.gcal/gadget                  :s.prop.gcal.evt/gadget
   :s.prop.gcal/anyoneCanAddSelf        boolean?
   :s.prop.gcal/guestsCanInviteOthers   boolean?
   :s.prop.gcal/guestsCanModify         boolean?
   :s.prop.gcal/guestsCanSeeOtherGuests boolean?
   :s.prop.gcal/privateCopy             boolean? ; r-o
   :s.prop.gcal/locked                  boolean? ; r-o
   :s.prop.gcal/reminders               :s.prop.gcal.evt/reminders
   :s.prop.gcal/source                  :s.prop.gcal.evt/source  ; only for the creator
   :s.prop.gcal/attachments             (s/coll-of :s.ent.gcal/attachment)})


(s/def ::evt-opt-keys
  (s/keys
    :opt-un
    [:s.prop.gcal/updated
     :s.prop.gcal/summary ; yep, can be untitled
     :s.prop.gcal/description
     :s.prop.gcal/location
     :s.prop.gcal/colorId
     :s.prop.gcal/creator
     :s.prop.gcal/organizer
     :s.prop.gcal/start
     :s.prop.gcal/end
     :s.prop.gcal/endTimeUnspecified
     :s.prop.gcal/transparency
     :s.prop.gcal/visibility
     :s.prop.gcal/iCalUID
     :s.prop.gcal/sequence
     :s.prop.gcal/attendees
     :s.prop.gcal/attendeesOmitted
     :s.prop.gcal/extendedProperties
     :s.prop.gcal/hangoutLink
     :s.prop.gcal/conferenceData
     :s.prop.gcal/gadget
     :s.prop.gcal/anyoneCanAddSelf
     :s.prop.gcal/guestsCanInviteOthers
     :s.prop.gcal/guestsCanModify
     :s.prop.gcal/guestsCanSeeOtherGuests
     :s.prop.gcal/privateCopy
     :s.prop.gcal/locked
     :s.prop.gcal/reminders
     :s.prop.gcal/source
     :s.prop.gcal/attachments
     :s.prop.gcal/eventType]))


(s/def :s.gcal.ent/event--regular
  (s/merge
    ::evt-opt-keys
    (s/keys :req-un [:s.prop.gcal/id
                     :s.gcal.prop.event/kind
                     :s.prop.gcal/status
                     :s.http/etag
                     :s.prop.gcal/htmlLink
                     :s.prop.gcal/created])))


(s/def :s.gcal.ent/event--regular-cancelled
  (s/keys :req-un [:s.prop.gcal/id
                   :s.prop.gcal.evt-cancelled/status]))

(s/def :s.gcal.ent/event--rec-base
  (s/merge
    ::evt-opt-keys
    (s/keys :req-un [:s.prop.gcal/id
                     :s.gcal.prop.event/kind
                     :s.prop.gcal/status
                     :s.http/etag
                     :s.prop/recurrence
                     :s.prop.gcal/htmlLink
                     :s.prop.gcal/created])))

(s/def :s.gcal.ent/event--rec-exception
  (s/merge
    ::evt-opt-keys
    (s/keys :req-un [:s.prop.gcal/id
                     :s.gcal.prop.event/kind
                     :s.prop.gcal/status
                     :s.http/etag
                     :s.prop.gcal/recurringEventId
                     :s.prop.gcal/originalStartTime])))


(s/def :s.gcal.ent/event--rec-exception-cancelled
  ; Cancelled exceptions of an uncancelled recurring event indicate that
  ; this instance should no longer be presented to the user. Clients should
  ; store these events for the lifetime of the parent recurring event.
  ; Cancelled exceptions are only guaranteed to have values for the id,
  ; recurringEventId and originalStartTime fields populated. The other fields might be empty.
  (s/keys :req-un [:s.prop.gcal/id
                   :s.prop.gcal.evt-cancelled/status
                   :s.prop.gcal/recurringEventId
                   :s.prop.gcal/originalStartTime]))


(s/def :s.gcal.ent/event
  (s/or ::regular                  :s.gcal.ent/event--regular
        ::regular-cancelled        :s.gcal.ent/event--regular-cancelled
        ::rec-base                 :s.gcal.ent/event--rec-base
        ::rec-exception            :s.gcal.ent/event--rec-exception
        ::rec-exception-cancelled  :s.gcal.ent/event--rec-exception-cancelled))


(s/assert
  :s.gcal.ent/event--regular
  {:iCalUID            "goog-event-id@google.com",
   :description        "Ivan Fedorov is inviting you to a scheduled Zoom meeting.",
   :eventType          "default",
   :creator            {:email "hello@example.io", :self true},
   :extendedProperties {:shared {:zmMeetingNum "75615174335"}},
   :updated            "2021-01-16T16:56:04.847Z",
   :conferenceData     {:entryPoints        [{:entryPointType "video",
                                              :uri            "https://meet.google.com/not-an-id",
                                              :label          "meet.google.com/not-an-id"}],
                        :conferenceSolution {:key     {:type "hangoutsMeet"},
                                             :name    "Google Meet",
                                             :iconUri "https://fonts.gstatic.com/s/i/productlogos/meet_2020q4/v6/web-512dp/logo_meet_2020q4_color_2x_web_512dp.png"},
                        :conferenceId       "not-an-id",
                        :signature          "AL9oL6XRix+Rb5sHtGh5M/DQk3e1"},
   :htmlLink           "https://www.google.com/calendar/event?eid=not-an-id",
   :start              {:dateTime "2021-01-12T09:00:00+03:00", :timeZone "Europe/Moscow"},
   :etag               "\"3221632329694000\"",
   :created            "2021-01-11T09:20:30.000Z",
   :summary            "LaMarr and Ivan zoom",
   :attendees          [{:email "example@gmail.com", :responseStatus "needsAction"}
                        {:email "hello@example.io", :organizer true, :self true, :responseStatus "accepted"}],
   :status             "confirmed",
   :id                 "not-an-id",
   :kind               "calendar#event",
   :sequence           1,
   :reminders          {:useDefault true},
   :end                {:dateTime "2021-01-12T09:40:00+03:00", :timeZone "Europe/Moscow"},
   :location           "https://us04web.zoom.us/j/smth?pwd=smth",
   :hangoutLink        "https://meet.google.com/not-an-id",
   :organizer          {:email "hello@example.io", :self true}})


