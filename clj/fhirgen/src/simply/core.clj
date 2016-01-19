(ns simply.core
  (:require
    [simply.formats :as sf]
    [simply.db :as db]
    [route-map :as rm]
    [simply.validation :as sv]
    [hiccup.page :as hp]
    [hiccup.core :as hc]
    [hiccup.util :as hu]
    [clojure.stacktrace :as cst]
    [ring.middleware.defaults :refer :all]
    [ring.middleware.resource :as rmr]
    [ring.middleware.json :as rmj]
    [ring.middleware.content-type :as rmc]
    [ring.middleware.not-modified :as rmn]
    [ring.middleware.anti-forgery :as rma]
    [crypto.password.scrypt :as password]
    [ring.util.response :as rur]))

(def validate sv/validate)

(defn html [cnt]
  (-> {:status 200
       :body cnt }
      (rur/content-type "text/html")))

(defn json
  "respond with json; opts hash merged into responce, i.e. {:status ???}"
  ([cnt] (json cnt {}))
  ([cnt opts]
   (-> {:status 200 :body (if (string? cnt) cnt (sf/to-json cnt))}
       (rur/content-type "application/json")
       (merge opts))))

(defn uuid  []  (str  (java.util.UUID/randomUUID)))

(defn redirect
  ([url]  (rur/redirect (str url)))
  ([url opts]  (->  (rur/redirect (str url))
                   (merge opts))))

(defn url
  "proxy hiccup.util/url"
  [& args] (apply hu/url args))

;; web

(defn transaction-mw [h]
  (fn [req]
    (db/transaction (h req))))

(defn exceptions-page [h]
  (fn [req]
    (try (h req)
         (catch Exception e
           (let [trace (with-out-str (cst/print-stack-trace e))]
             (println "Error:" trace)
             (json {:message (str e) :trace trace} {:status 500}))))))

(defn build-stack
  "wrap h with middlewares mws"
  [h mws]
  ((apply comp mws) h))

(defmacro <- [& xs]
  (apply list '->
         (last xs)
         (reverse (butlast xs))))

(defmacro defintercept [nm args err pred]
  `(defn ~nm [h#]
     (fn [req#]
       (let [~args [req#]]
         (if-let [res# ~pred]
           (h# (assoc req# ~(keyword nm) res#))
           (json {:message ~(:message err)} {:status ~(:status err)}))))))

(defn match-route [routes meth path]
  (rm/match [meth path] routes))

(defn resolve-route [h routes]
  (fn [{uri :uri meth :request-method :as req}]
    (if-let [route (match-route routes meth uri)]
      (h (assoc req :route route))
      (-> (rur/response (hp/html5 [:html [:body [:center [:h1 404 ":("] [:h3 (str "[" meth " " uri  "] not found")]]]]))
          (rur/content-type "text/html")
          (rur/status 404)))))

(defn collect-mw [match]
  (->> (conj (:parents match) (:match match))
       (mapcat :mw)
       (filterv (complement nil?))))

(defn dispatch [routes]
  (-> (fn [{handler :handler route :route :as req}]
        (let [mws     (collect-mw route)
              handler (get-in route [:match])
              req     (update-in req [:params] merge (:params route))]
          (println "Dispatching " (:request-method req) " " (:uri req) " to " (pr-str handler))
          (println "Middle-wares: "   (pr-str mws))
          ((build-stack handler mws) req)))
      (resolve-route routes)))


(defn log-request [h]
  (fn [req]
    (println "\n\n->>" (:request-method req) " " (:uri req))
    (let [res (time (h req))]
      (println "<<-" (:request-method req) " " (:uri req) "\n-------------------")
      res)))

(defn wrap-layout  [h layout-ns]
  (require layout-ns)
  (fn  [req]
    (let  [resp  (h req)]
      (if  (vector? resp)
        (let  [layout-fn-name  (or  (:layout  (meta resp)) '-layout)
               layout-fn  (ns-resolve layout-ns  (symbol layout-fn-name))]
          (if-not layout-fn
            (html  (str "Ups no layout-fn - " layout-ns "/" layout-fn-name))
            (html  (layout-fn req resp))))
        resp))))


(defn site [h opts]
  (-> h
      #_(transaction-mw)
      (wrap-defaults (merge-with merge api-defaults opts))
      (rmj/wrap-json-response)
      (rmj/wrap-json-body {:keywords? true})
      (rmr/wrap-resource "public")
      (rmc/wrap-content-type)
      (rmn/wrap-not-modified)
      (exceptions-page)
      (log-request)))

(defn encrypte-password [x] (password/encrypt x))
(defn check-password [raw encrypted] (password/check raw encrypted))
