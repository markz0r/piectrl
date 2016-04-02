(ns piectrl.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(defonce timer-data (reagent/atom {:ttl 0 :status 0}))

;; ############################################
;; AJAX
;; ############################################

(defn handler [response] (.log js/console (str response))
    (if (= (response :status) 0)
        (reset! timer-data {:ttl 0 :status 0})
        (reset! timer-data {:ttl (.toFixed ( / ( - (response :ttl) (.getTime (js/Date.))) 60000)) :status 1})))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console
    (str "something bad happened: " status " " status-text)))

(defn send-post [loc params-list] (POST loc
        {:headers {"Accept" "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "csrf-token"))}
         :params params-list
         :handler handler
         :error-handler error-handler}))

(defn set-status-data [id ttl status] (send-post "/set-state"
                                 {:id id
                                  :ttl ttl
                                  :status status}))

(defn get-status-data [id] (send-post "/get-state"
                                {:id id}))

(def data-updater (js/setInterval
                       #(get-status-data 17)1000))
                         ;reset! timer (js/Date.)) 5000))
;; ############################################
;; COMPONENTS
;; ############################################
(defn slider [param value min max id]
  [:input {:type "range" :value value :min min :max max :id id
           :style {:width "95%" :text-align "center"}
           :on-change #(reset! (timer-data :ttl) (-> % .-target .-value))
           :on-mouse-up #(set-status-data id (@timer-data :ttl)(@timer-data :status))}])

(defn switcher [id]
  [:input {:type "button" :value (if (= (@timer-data :status) 1)"SWITCH OFF" "SWITCH ON") :id id
           :style {:width "20%" :text-align "center"}
           (swap! m1 assoc :a "Aaay")
           :on-mouse-up #((swap! timer-data assoc :status (if (= (@timer-data :status) 1) 0 1))
                          (set-status-data id (@timer-data :ttl) (@timer-data :status)))}])

(defn nav-link [uri title page collapsed?]
  [:li {:class (when (= page (session/get :page)) "active")}
   [:a {:href uri
        :on-click #(reset! collapsed? true)}
    title]])

(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle
          {:class         (when-not @collapsed? "collapsed")
           :data-toggle   "collapse"
           :aria-expanded @collapsed?
           :aria-controls "navbar"
           :on-click      #(swap! collapsed? not)}
          [:span.sr-only "Toggle Navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href "#/"} "piectrl"]]
        [:div.navbar-collapse.collapse
         (when-not @collapsed? {:class "in"})
         [:ul.nav.navbar-nav
          [nav-link "#/" "Home" :home collapsed?]]]]])))

;; ############################################
;; PAGES
;; ############################################
(defn home-page []
  [:div.container
   [:div.jumbotron
   ;; add dials n shit
    [:h4 "Sprinkler"]
    [:div
      (@timer-data :ttl) " mins remaining"
      [slider timer-data (@timer-data :ttl) 0 180 17]]
    [:div
      [switcher 17]]
    ]])

(def pages
  {:home #'home-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))
