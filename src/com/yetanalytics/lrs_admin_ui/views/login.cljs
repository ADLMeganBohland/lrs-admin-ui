(ns com.yetanalytics.lrs-admin-ui.views.login
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [clojure.pprint :refer [pprint]]
   [com.yetanalytics.lrs-admin-ui.functions :as fns]))

(defn login []
  [:main {:class "page-login"}
   [:div {:class "opacity-wrapper"}]
   [:div {:class "main-wrapper"}
    [:div {:class "logo-image"}
     [:i
      [:img {:src "images/logo.png", :alt "LRS Logo"}]]]
    [:h1 "Login"]
    [:div {:class "form-wrapper"}
     [:form {:id "login-form"}
      [:div {:class "form-group"}
       [:label {:class "field-label",
                :for "username"}
        "Username"]
       [:input {:type "text",
                :class "form-control",
                :name "username",
                :value @(subscribe [:login/get-username])
                :on-change #(dispatch [:login/set-username
                                       (fns/ps-event-val %)])
                :id "username"}]]
      [:div {:class "form-group"}
       [:label {:class "field-label",
                :for "password"}
        "Password"]
       [:input {:type "password",
                :class "form-control",
                :name "password",
                :value @(subscribe [:login/get-password])
                :on-change #(dispatch [:login/set-password
                                       (fns/ps-event-val %)])
                :id "password"}]]
      ;;To revisit when functionality is possible
      #_[:div {:class "form-group text-right", :id "forgot-pwd"}
       [:a {:class "fg-primary font-rem-80", :href "#"} "Forgot your password? "
        [:span {:class "fg-white"} "Reset password"]]]
      [:div {:class "login-button-wrapper"}
       [:div
        [:button {:class "login-button"
                  :on-click (fn [e]
                              (fns/ps-event e)
                              (dispatch [:session/authenticate]))} "LOGIN"]]
       [:div
        [:a {:class "btn-alternate-login", :href "#"} "No account? Create an account"]]
       [:p {:class "login-separator"} "OR"]
       [:div
        [:button {:class "btn-alternate-login"} "Login with AWS"]]]]]]])
