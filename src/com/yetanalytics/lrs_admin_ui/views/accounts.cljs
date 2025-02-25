(ns com.yetanalytics.lrs-admin-ui.views.accounts
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [subscribe dispatch]]
   [com.yetanalytics.lrs-admin-ui.functions :as fns]
   [com.yetanalytics.lrs-admin-ui.functions.copy :refer [copy-text]]
   [com.yetanalytics.lrs-admin-ui.input :refer [p-min-len u-min-len]]
   [com.yetanalytics.lrs-admin-ui.functions.password :as pass]
   [goog.string                                      :refer [format]]
   goog.string.format))

(defn account [{{:keys [_ username] :as account} :account}]
  (let [delete-confirm (r/atom false)
        current-username @(subscribe [:session/get-username])]
    (fn []
      [:li {:class "mb-2"}
       [:div {:class "accordion-container"}
        [:div {:class "account-row"}
         [:div {:class "account-col"}
          [:p username]]
         [:div {:class "account-col"}
          [:ul {:class "action-icon-list"}
           (if @delete-confirm
             [:li
              [:span "Are you sure?"]
              [:a {:href "#!",
                   :on-click #(do (dispatch [:accounts/delete-account account])
                                  (swap! delete-confirm not))
                   :class "confirm-delete"}
               "Yes"]
              [:a {:href "#!"
                   :on-click #(swap! delete-confirm not)
                   :class "confirm-delete"}
               "No"]]
             [:li
              [:a {:href "#!"
                   :on-click #(swap! delete-confirm not)
                   :class "icon-delete"}
               "Delete"]])
           (when (= username current-username)
             [:li
              [:a {:href "#!"
                   :on-click (fn [e]
                               (fns/ps-event e)
                               (dispatch [:session/set-page :update-password]))
                   :class "icon-edit"}
               "Update Password"]])]]]]])))

(defn new-account []
  (let [hide-pass (r/atom true)]
    (fn []
      (let [new-account @(subscribe [:db/get-new-account])]
        [:div {:class "create-account-inputs"}
         [:div {:class "row"}
          [:label {:for "new-username-input"} "Username:"]
          [:input {:value (:username new-account)
                   :class "new-account round"
                   :id "new-username-input"
                   :on-change #(dispatch [:new-account/set-username (fns/ps-event-val %)])}]]
         [:span {:class "username-note"}
          (format "Username must be %d or more alphanumeric characters" u-min-len)]
         [:div {:class "row pt-2"}
          [:label {:for "new-password-input"} "Password:"]
          [:input (cond-> {:value (:password new-account)
                           :class "new-account round"
                           :id "new-password-input"
                           :on-change #(dispatch [:new-account/set-password
                                                  (fns/ps-event-val %)])}
                    @hide-pass (merge {:type "password"}))]
          [:ul {:class "action-icon-list"}
           [:li
            [:a {:href "#!",
                 :class "icon-secret pointer"
                 :on-click #(swap! hide-pass not)}
             (str (cond
                    @hide-pass "Show"
                    :else "Hide"))]]
           [:li
            [copy-text
             {:text (:password new-account)
              :on-copy #(dispatch [:notification/notify false
                                   "Copied New Password!"])}
             [:a {:class "icon-copy pointer"} "Copy"]]]]]
         [:span {:class "password-note"}
          (format "Password must be %d or more characters and contain uppercase, lowercase, numbers, and special characters (%s). Be sure to note or copy the new password as it will not be accessible after creation."
                  p-min-len
                  (apply str pass/special-chars))]
         [:div {:class "row"}
          [:ul {:class "action-icon-list"}
           [:li
            [:a {:href "#!",
                 :on-click #(dispatch [:new-account/generate-password])
                 :class "icon-gen"} [:i "Generate Password"]]]]]]))))

(defn accounts []
  (dispatch [:accounts/load-accounts])
  (let [accounts @(subscribe [:db/get-accounts])]
    [:div {:class "left-content-wrapper"}
     [:h2 {:class "content-title"}
      "Account Management"]
     ;; this will be looped for all tenants if tenant mode is enabled (third)
     [:div {:class "tenant-wrapper"}
      [:div {:class "accounts-table-header"} "Account"]
      [:ol {:class "accounts-list accordion"}
       ;; will repeat for each key
       (map
        (fn [{:keys [account-id] :as acct}]
          [account {:account acct
                    :key (format "account-item-%s" account-id)}])
        accounts)]
      [:div {:class "h-divider"}]
      [:h3 {:class "content-title"} "Create New Account"]
      [new-account]
      [:div {:class "accounts-table-actions"}
       [:input {:type "button",
                :class "btn-blue-bold",
                :on-click #(dispatch [:accounts/create-account])
                :value "CREATE ACCOUNT"}]]]]))
