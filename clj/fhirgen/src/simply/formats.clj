(ns simply.formats
  (:require
   [cheshire.core :as cc]
   [clj-time.format :as tfmt]
   [schema.utils :as s-utils]
   [cheshire.generate :as cg]))


(cg/add-encoder
 schema.utils.ValidationError
 (fn [validation-error jsonGenerator]
   (cg/encode-seq (s-utils/validation-error-explain validation-error) jsonGenerator)))

(def date-to-json-formatter
  (tfmt/formatters :date-time))

(cg/add-encoder
 org.joda.time.DateTime
 (fn  [d json-generator]
   (.writeString json-generator
                 (tfmt/unparse date-to-json-formatter d))))

(defn from-json  [str]
  (cc/parse-string str keyword))

(defn to-json  [clj &  [options]]
  (cc/generate-string clj options))

