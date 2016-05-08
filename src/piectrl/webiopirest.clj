(ns piectrl.webiopirest
  (:use [overtone.at-at])
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))
;; ############################################
;; constants and atoms
;; ############################################
(def kill-pool (mk-pool))
(def pi-info (env :pi-info))
(def pi-ttl (atom 0))
;; ############################################
;; Build and send requests
;; ############################################
(defn req-build [id &[newVal]]
  (str/join "" [(pi-info :url) "GPIO/" id "/value" newVal]))

(defn req-send [fn url]
  (try
    (def response (fn url {:basic-auth [(pi-info :user) (pi-info :password)]
                           :socket-timeout 4000 :conn-timeout 4000
                           :throw-exceptions true}))
  (catch Exception e
    (log/error (str/join " " ["There was an issue connecting to" url])))))

  (defn get-GPIO-status [id]
   (def response_all (req-send client/get (req-build id)))
    {:id id
    :status (if (nil? response_all) "-1" (response_all :body))})

(defn get-all-GPIO [] (map #(get-GPIO-status %1) (pi-info :GPIOs)))

(defn def-mapper [id] {:id id :status 0})
(def pi-atom (atom (map #(def-mapper %1) (pi-info :GPIOs))))

(defn update-pi-atom []
  (reset! pi-atom (get-all-GPIO))
  (log/debug "updating from pi source"))

(defn set-GPIO [id state]
  ((req-send client/post
              (req-build id (str/join "" ["/" state]))) :body))

(defn turn-off-all [] (set-GPIO 17 0))

;; ############################################
;; Timer function
;; ############################################
(defn reset-pool [pool] (stop-and-reset-pool! pool))
(defn show-sched [pool] (show-schedule pool))

(defn send-kill [] (println "sending off sig")
  (reset-pool kill-pool)(reset! pi-ttl 0)
  (turn-off-all))

(defn death-task [ms-tl]
  (at (+ ms-tl (now)) (send-kill) kill-pool))

;; ############################################
;; UI function
;; ############################################
(defn set-ttl-int [new-val id]
  (reset! pi-ttl (+ (* 60000 (read-string new-val))(System/currentTimeMillis)))
  (reset-pool kill-pool)(death-task @pi-ttl)(set-GPIO id 1))

(defn update-state [id new-val status]
  (if (= new-val "0")
        (if (= status 0)(send-kill)((set-GPIO id 1)(reset! pi-ttl 0)(reset-pool kill-pool)))
    ; If new ttl is not 0 update interal timer
        (set-ttl-int new-val id)))
