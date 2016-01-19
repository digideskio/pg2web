(ns fhirgen.core
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]  
            [faker.name :as name]
            [honeysql.core :as sql]
            [simply.formats :as json]  
            [faker.address :as address]
            [faker.phone-number :as phone]
            [faker.company :as company]
            [faker.lorem :as lorem]))

(defn mk-address []
  {:line [(address/street-address), (address/street-name)],
   :city (address/city),
   :state (address/us-state-abbr),
   :postalCode (address/zip-code),
   :country "USA"})

(defn mk-name []
  {:given [(name/first-name)],
   :middle [(name/prefix)],
   :family [(name/last-name)],
   :text "JEAN SANDY JACK"})

(defn rand-year []
  (+ 1940 (rand-int 70)))

(rand-year)


(defn gen-uuid []
  (.toString  (java.util.UUID/randomUUID)))


(defn mk-id [] (gen-uuid))

(defn mk-phone []
  (first (phone/phone-numbers)))

(defn mk-identifier []
  (first (phone/phone-numbers)))

(defn mk-telecome []
  {:use "home"
   :value (mk-phone)
   :system "phone"})

(defn mk-contact []
  {:gender "unknown",
   :name (merge (mk-name) {:use "official"}),
   :address {:use "Mailing Address"}})

(defn mk-pt-ids []
  [{:value "010107112",
    :system "urn:oid:2.16.840.1.113883.4.3.42",
    :type {:text "External ID"}}
   {:value "160923",
    :system "urn:oid:2.16.840.1.113883.4.3.45",
    :type {:text "Internal ID"}}
   {:value "160923",
    :system "urn:oid:2.16.840.1.113883.4.3.46",
    :type {:text "Alternate ID"}}
   {:value "5435435435",
    :type {:text "Account number"}}
   {:value "123-22-1111",
    :system "http://hl7.org/fhir/sid/us-ssn",
    :type {:text "Social Security Number"}}])

(defn mk-patient []
  {:address [(mk-address)],
   :deceasedBoolean "N",
   :name [(mk-name)],
   :birthDate (str (rand-year) "-10-12T00:00:00.000Z"),
   :resourceType "Patient",
   :id (mk-id),
   :identifier (mk-pt-ids),
   :telecom [(mk-telecome)],
   :gender "female",
   :contact [(mk-contact)]})


(defn resource-ref [res] {:reference (str (:resourceType  res) "/" (:id res))})

(defn mk-coverage [pt]
  {:id (mk-id)
   :subscriberLastName (get-in pt [:name 0 :family 0]),
   :policyNumber "992001",
   :subscriberMiddleName "S",
   :subscriberDOB (:birthDate pt),
   :name "MEDICARE IP",
   :effectiveDate "1995-11-01T00:00:00.000Z",
   :resourceType "Coverage",
   :subscriber (resource-ref pt)
   :identifier (:identifier pt),
   :subscriberFirstName (get-in pt [:name 0 :given 0])})


(defn mk-company-name []
  (first (company/names)))

(defn mk-location []
  {:id (mk-id)
   :resourceType "Location",
   :status "active",
   :identifier [{:value (mk-identifier)}],
   :building "S",
   :room "101",
   :bed "1",
   :name (mk-company-name)})

(defn mk-practitioner []
  {:id (mk-id)
   :resourceType "Practitioner",
   :identifier [{:value (mk-identifier)}],
   :name {:given [(name/first-name)],
          :middle [(name/prefix)],
          :family [(name/last-name)]}})

(defn mk-encounter [pt atnd adm loc]
  {:id (mk-id)
   :resourceType "Encounter",
   :patient (resource-ref pt)
   :participant [{:type {:coding [{:code "ATND",:system "http://hl7.org/fhir/v3/ParticipationType"}]},
                  :individual (resource-ref atnd)}
                 {:type {:coding [{:code "ADM",:system "http://hl7.org/fhir/v3/ParticipationType"}]},
                  :individual (resource-ref adm)}],
   :status "planned",
   :class "I",
   :identifier [{:value (mk-identifier)}],
   :period {:start "2016-01-23T21:06:33.000Z"},
   :location {:location (resource-ref loc) :status "active"}})

(defn mk-report [pt]
  {:id (mk-id)
   :resourceType "DiagnosticReport"
   :issued "2011-12-15T20:06:00.000Z"
   :subject (resource-ref pt)
   :conclusion "SCREENING LUNG^^^^^From : Alphonsus Regional Center^^^^^2, Saint Alphonsus Gem State^^^^^Medical Center Radiolgy^^^^^BAKER CHTY^^^^^Patient: - DOB: - EMPI: - visitiAcct: sassassam. 3 Site:^^^^^PTIMOD: - MiFN:^^^^^3 Ref. Prov: Exam: - Room/Bed: CTB Add. Providers:^^^^^SCREENING LUNG \"^^^^^ΕΧΑΜΙ DΑΤΕ: 6/16/2015 13:45 Contrast/Isotope: None^^^^^PROCEDURE: CT LUNG LOW DOSE CA SCREENING^^^^^COMPARISON: None.^^^^^INDICATIONS: 64 year old former smoker with 50 pack year history of smoking. Quit April 2015.^^^^^PQRS QUALITY INFORMATION:^^^^^EXAM COUNT: 0 CT exams and 0 NM Myocardial Perfusion exams in past 12 months.^^^^^DATA AVAILABILITY: Dicom format image data is available to non-affiliated external entities on secure, media free, reciprocally searchable basis for 12^^^^^month period of time after the study.^^^^^DATA SEARCH. A search was conducted for prior patient CT exams performed at an external facility within 12 months prior to the imaging study being^^^^^performed, and is available through a secure, authorized, media free, shared archive.^^^^^CT DOSE REGISTRY: CT study data has been reported to a CT Radiation Dose Index Registry containing all minimum necessary data elements.^^^^^Total Dose Length Product (DLP in mGy-cm): 38.50^^^^^TECHNIQUE: CT images of the lungs were obtained without intravenous contrast and utilizing a very low dose technique.^^^^^LUNG FINDINGS:^^^^^NODULES: Noncalcified linear focus in the lateral basilar right lower lobe seen on series 9^^^^^image 185 measures 0.3 x 0.2 cm. Punctate calcified nodule in the lingula seen on series 9 image 175. Calcified nodule noted in the apical posterior segment left upper lobe, series 9 image 79.^^^^^LUNG PARENCHYMA: Within normal limits.^^^^^OTHER FINDINGS:^^^^^HEART: Within normal limits. MEDIASTINUM/HILA: Within normal limits. VASCULATURE: Within normal limits. PLEURAL SPACE: Within normal limits.^^^^^CHEST WALL/AXILLA: Within normal limits. LTD, UPPER ABDOMEN: Within normal limits. MUSCULOSKELETAL: Normal for age. ADIOT\"L COMMENTS: None.^^^^^CONCLUSION: Solitary linear noncalcified right lower lobe pulmonary nodule measures less than 4 mm^^^^^average diameter and demonstrates benign pattern morphology.^^^^^RECOMMENDATION: LungPADS 1 - follow up low dose chest CT in 1 year."
   :status "P"
   :serviceCategory {:coding [{:code "CT" :display "Computed Tomography"}]}
   :code {:coding [{:code "4550422" :display "CT ANGIO NECK W WO CON"}]}})

(defn mk-notes [pt]
  {:id (mk-id)
   :subject (resource-ref pt)
   :resourceType "Basic"
   :issued "2016-01-04T12:45:00.000Z"
   :code {:code "instruct" :display "Instructions"}
   :text (str/join " " (take 20 (lorem/words)))})

(defn to-json [x] 
  (str/replace (json/to-json x) #"'" "''"))

(defn wrap-insert [res]
  (str "insert into " (str/lower-case (:resourceType res))
       " (id, resource_type, resource) VALUES " 
       "('" (:id res) "','" (:resourceType res) "','" (to-json res) "');" ))

(defn patient-bundle []
  (let [pt (mk-patient)
        atnd (mk-practitioner)
        adm (mk-practitioner)
        loc (mk-location)]
     [(wrap-insert pt)
      (wrap-insert (mk-coverage pt))
      (wrap-insert atnd)
      (wrap-insert adm)
      (wrap-insert loc)
      (wrap-insert (mk-notes pt))
      (wrap-insert (mk-encounter pt atnd adm loc))
      (wrap-insert (mk-report pt))]))



(def file "/home/aitem/Work/pg2web/seed.sql")

(comment 
  (pp/pprint (wrap-insert (mk-patient)))
  (do  
    (spit file (str/join "\n"  
      ["SET plv8.start_proc = 'plv8_init';"
       "SELECT fhir_create_storage('{\"resourceType\": \"Patient\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"Coverage\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"Practitioner\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"Location\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"Basic\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"Encounter\"}'::json);"
       "SELECT fhir_create_storage('{\"resourceType\": \"DiagnosticReport\"}'::json);\n"]))

    (dotimes [n 100]
      (spit file (str/join "\n" (patient-bundle) ):append true)))


  (pp/pprint (to-json (mk-patient)))
  (keyword (str/lower-case (:resourceType (mk-patient))))

  (str/replace "The color is ' red" #"'" "''")
  (mod (int (+ 110 (rand 100))) 100)

  
  )



