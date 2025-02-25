(ns com.yetanalytics.lrs-admin-ui.views.browser
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   [com.yetanalytics.lrs-admin-ui.functions :as fns]
   [com.yetanalytics.lrs-admin-ui.functions.http :as httpfn]
   [clojure.string :refer [blank?]]))

;; Credential scopes to allow the browser to use (those which can read)
(def read-scopes #{"all"
                   "all/read"
                   "state"
                   "statements/read"
                   "statements/read/mine"})

(defn process-click
  "Extract the pertinent parts of an element from an event and instrument links
  with appropriate dispatch. ignore non-links and external links"
  [event]
  (let [elem (.-target event)]
    (when (and (= (.-nodeName elem) "A")
               (httpfn/is-rel? (.-href elem)))
      ;;prevent nav
      (fns/ps-event event)
      ;;dispatch xapi parsing and load
      (dispatch [:browser/load-xapi {:path   (.-pathname elem)
                                     :params (.-search elem)}]))))

(defn browser []
  (let [filter-expand (r/atom false)]
    (fn []
      (let [content @(subscribe [:browser/get-content])
            ;;filter out credentials that can't read the LRS
            read-credentials (filter #(some read-scopes (:scopes %))
                                     @(subscribe [:db/get-credentials]))]
        [:div {:class "left-content-wrapper"}
         [:h2 {:class "content-title"}
          "Data Browser"]
         [:p
          [:span
           [:b "Credentials to Use: "]
           [:select {:name (str "update_credential")
                     :on-change #(dispatch [:browser/update-credential
                                            (fns/ps-event-val %)])}
            [:option "Credential to Browse"]
            (map-indexed
             (fn [idx credential]
               [:option {:value (:api-key credential)
                         :key (str "browser-credential-" idx)}
                (fns/elide (:api-key credential) 20)])
             read-credentials)]
           ]]
         (let [address @(subscribe [:browser/get-address])
               params  (httpfn/extract-params address)]
           (when (some? address)
             [:div {:class "browser-filters"}
              [:p "Current Query:"]
              [:div {:class "xapi-address"}
               address]
              (when (not-empty params)
                [:div {:class "filters-wrapper"}
                 [:span {:class (str "pointer collapse-sign"
                                     (when @filter-expand " expanded"))
                         :on-click #(swap! filter-expand not)}
                  "Filters:"]
                 (when @filter-expand
                   [:div
                    [:div {:class "filter-table"}
                     (map
                      (fn [[key val]]
                        [:div {:class "filter-row"}
                         [:div {:class "filter-column key"}
                          key]
                         [:div {:class "filter-column"}
                          val]])
                      (seq params))]
                    [:ul {:class "action-icon-list"}
                     [:li
                      [:a {:href "#!"
                           :on-click #(dispatch [:browser/load-xapi])
                           :class "icon-clear-filters"} "Clear Filters"]]]])])]))
         (if (blank? content)
           [:div {:class "browser"}
            "Please Choose an API Key Above to Browse LRS Data"]
           [:div {:class "browser"
                  :on-click process-click
                  "dangerouslySetInnerHTML" #js{:__html content}}])]))))
