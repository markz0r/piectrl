(ns piectrl.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [piectrl.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[piectrl started successfully using the development profile]=-"))
   :middleware wrap-dev})
