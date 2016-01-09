(ns piectrl.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[piectrl started successfully]=-"))
   :middleware identity})
