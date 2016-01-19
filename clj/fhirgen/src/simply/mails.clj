(ns simply.mails
  (:require
   [hiccup.core :as hc]
   [postal.core :as pc]
   [environ.core :as env]))

(def messages (atom []))
(defn fake-send-message [cfg msg] (swap! messages conj msg))

(defn send-message [cfg msg] (pc/send-message cfg msg))

(defmacro fake-mail [& body]
  `(with-redefs [send-message fake-send-message]
     (reset! messages []) 
     ~@body
     @messages))

;; TODO: use enveron
(defn mail-to [ml]
  "Send email ml=> {:keys [from to subject body] :or {to "" subject ""} :as msg}]"
  (send-message
   {:host (env/env :smtp-host)
    :port (Integer. (env/env :smtp-port "25"))
    :user (env/env :smtp-user )
    :pass (env/env :smtp-password)
    :ssl  (not (nil? (env/env :smtp-ssl)))}
     (merge ml
            {:from (env/env :smtp-email)
             :body [{:type "text/html; charset=utf-8" :content (:body ml)}]})))



(comment
  (mail-to {:to ["niquola@gmail.com"] :subject "test" :body [:div [:h1 "Hello"] [:a {:href "http://google.com"} "Ups"]]}))

(comment
  (mail-to {:to ["murad-bei@yandex.ru"] :subject "Проверка кириллицы" :body "Кириллица"})
  (mail-to {:to ["muradbei@gmail.com"] :subject "Проверка кириллицы" :body "Кириллица"})
  )

(comment
  (mail-to {:to ["bodnarchuk@gmail.com"] :subject "test" :body [:div [:h1 "Hello"] [:a {:href "http://google.com"} "Ups"]]}))
