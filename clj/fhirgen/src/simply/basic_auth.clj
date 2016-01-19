(ns simply.basic-auth
  (:import org.apache.commons.codec.binary.Base64
           org.apache.commons.codec.net.URLCodec)
  (:require
    [simply.util :as u]
    [simply.core :as sc]))

(defn bearer-token [key secret]
  (u/base64-encode (str (u/url-encode key) ":" (u/url-encode secret))))

(defn parse-bearer-token [token]
  (map u/url-decode
       (-> (u/base64-decode token)
           (clojure.string/split #":"))))

(defn parse-authorization-header [header]
  (->
    (clojure.string/replace header #"^Basic " "")
    (parse-bearer-token)))

(defn mk-authorization-header [client]
  (str "Basic "
       (bearer-token (:client_id client)
                     (:client_secret client))))
