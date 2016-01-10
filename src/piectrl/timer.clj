(ns piectrl.timer
   (:require [clojurewerkz.quartzite.scheduler :as sched]
             [clojurewerkz.quartzite.triggers :as trig]
             [clojurewerkz.quartzite.jobs :as job]
             [clojurewerkz.quartzite.schedule.simple :refer [schedule with-repeat-count with-interval-in-milliseconds]]
             [piectrl.webiopirest :as webiopi]
))

;; ############################################
;; Jobs
;; ############################################
(job/defjob exec-sched-job
  [ctx]
  (webiopi/update-pi-atom)
  (println (quot (System/currentTimeMillis) 1000)))

(defn get-ts [] (println (quot (System/currentTimeMillis) 1000)))

;; ############################################
;; Timers
;; ############################################
(def sched-keys {:job-key "jobs.noop.1", :trig-key "triggers.1"})
(def pi-scheduler (sched/initialize)) ;sched/start))
(def pi-job (job/build
              (job/of-type exec-sched-job)
              (job/with-identity (job/key (sched-keys :job-key)))))
(def pi-trigger (trig/build
                  (trig/with-identity (trig/key (sched-keys :trig-key)))
                  (trig/start-now)
                  (trig/with-schedule(schedule
                                     (with-repeat-count 10)
                                     (with-interval-in-milliseconds 5000)))))


(defn start-updater [] (
  (sched/schedule pi-scheduler pi-job pi-trigger)(sched/start)))

(defn kill-all [] (sched/shutdown pi-scheduler))
  ;(sched/delete-trigger pi-scheduler (sched-keys :trig-key))
                  ;(sched/delete-job pi-scheduler (sched-keys :job-key)))
