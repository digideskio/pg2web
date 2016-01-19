(ns simply.util
  (:import org.apache.commons.codec.binary.Base64
           org.apache.commons.codec.net.URLCodec)
  (:require
    [simply.core :as sc])
  (:import  [java.net.URI]))

(defn to-utf  [s]
  (.getBytes s "UTF-8"))

(def url-codec (URLCodec.))

(defn url-encode [s]
  (.. url-codec (encode (str s)) (replaceAll "\\+" "%20")))

(defn url-decode [s]
  (. url-codec (decode s)))

(defn base64-encode [x]
  (String.  (Base64/encodeBase64  (to-utf x))))

(defn base64-decode [x]
  (String.  (Base64/decodeBase64  (to-utf x))))

(defn get-host [x]
  (try (.getHost (java.net.URI. x))
      (catch Exception e nil)))
