(ns onyx.scheduler.colocation-test
  (:require [clojure.test :refer :all]
            [onyx.scheduling.common-job-scheduler :refer [reconfigure-cluster-workload]]
            [onyx.log.generators :refer [one-group]]
            [onyx.api]))

;; Tests are :broken due to get-peer-site and inability to pass in peer-config
(deftest ^:broken colocate-tasks-on-a-single-machine
  (is
   (=
    {:j1 {:t1 [:p4] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :b}
                            :p4 {:aeron/external-addr :a}
                            :p5 {:aeron/external-addr :b}}})] 
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken refuse-to-run-job-if-machine-not-big-enough
  (is
   (= {}
      (:allocations
       (let [r (one-group
                {:messaging {:onyx.messaging/impl :aeron}
                 :job-scheduler :onyx.job-scheduler/greedy
                 :task-schedulers {:j1 :onyx.task-scheduler/colocated}
                 :peers [:p1 :p2 :p3 :p4 :p5]
                 :jobs [:j1]
                 :tasks {:j1 [:t1 :t2 :t3]}
                 :peer-sites {:p1 {:aeron/external-addr :a}
                              :p2 {:aeron/external-addr :a}
                              :p3 {:aeron/external-addr :b}
                              :p4 {:aeron/external-addr :c}
                              :p5 {:aeron/external-addr :b}}})]
         (reconfigure-cluster-workload r r))))))

(deftest ^:broken colocate-on-two-machines
  (is
   (=
    {:j1 {:t1 [:p3 :p6] :t2 [:p2 :p4] :t3 [:p1 :p5]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :b}}})]
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken ban-small-machines
  (is
   (=
    {:j1 {:t1 [:p3 :p6] :t2 [:p2 :p4] :t3 [:p1 :p5]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6 :p7 :p8]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :b}
                            :p7 {:aeron/external-addr :c}
                            :p8 {:aeron/external-addr :c}}})]
     (reconfigure-cluster-workload r r))))))

(deftest ^:broken colocate-on-three-machines
  (is
   (=
    {:j1 {:t1 [:p3 :p6 :p7]
          :t2 [:p2 :p4 :p9]
          :t3 [:p1 :p5 :p8]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6 :p7 :p8 :p9]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :b}
                            :p7 {:aeron/external-addr :c}
                            :p8 {:aeron/external-addr :c}
                            :p9 {:aeron/external-addr :c}}})] 
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken one-peer-not-in-multiple-not-used
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/balanced
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :a}}})] 
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken two-peers-not-in-multiple-not-used
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/balanced
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :a}
                            :p5 {:aeron/external-addr :a}}})]
       (reconfigure-cluster-workload r r ))))))

(deftest ^:broken greedy-job-scheduler-pins-to-colocated-job
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated
                                 :j2 :onyx.task-scheduler/balanced}
               :peers [:p1 :p2 :p3 :p4 :p5]
               :jobs [:j1 :j2]
               :tasks {:j1 [:t1 :t2 :t3]
                       :j2 [:t4 :t5]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}}})]
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken smaller-machines-are-dismissed
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :c}}})] 
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken greedy-scheduler-excludes-other-elligible-jobs
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated
                                 :j2 :onyx.task-scheduler/balanced}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6]
               :jobs [:j1 :j2]
               :tasks {:j1 [:t1 :t2 :t3]
                       :j2 [:t4 :t5]}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :c}}})] 
       (reconfigure-cluster-workload r r))))))

(deftest ^:broken balanced-scheduler-makes-room-for-second-job
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}
     :j2 {:t4 [:p5 :p6] :t5 [:p4]}}
    (:allocations
     (reconfigure-cluster-workload
      (one-group
       {:messaging {:onyx.messaging/impl :aeron}
        :job-scheduler :onyx.job-scheduler/balanced
        :task-schedulers {:j1 :onyx.task-scheduler/colocated
                          :j2 :onyx.task-scheduler/balanced}
        :peers [:p1 :p2 :p3 :p4 :p5 :p6]
        :jobs [:j1 :j2]
        :tasks {:j1 [:t1 :t2 :t3]
                :j2 [:t4 :t5]}
        :peer-sites {:p1 {:aeron/external-addr :a}
                     :p2 {:aeron/external-addr :a}
                     :p3 {:aeron/external-addr :a}
                     :p4 {:aeron/external-addr :b}
                     :p5 {:aeron/external-addr :b}
                     :p6 {:aeron/external-addr :c}}}))))))

(deftest ^:broken obeys-min-peers-constraint
  (is
   (=
    {:j1 {:t1 [:p3] :t2 [:p2] :t3 [:p1]}}
    (:allocations
     (let [r (one-group
              {:messaging {:onyx.messaging/impl :aeron}
               :job-scheduler :onyx.job-scheduler/greedy
               :task-schedulers {:j1 :onyx.task-scheduler/colocated}
               :peers [:p1 :p2 :p3 :p4 :p5 :p6]
               :jobs [:j1]
               :tasks {:j1 [:t1 :t2 :t3]}
               :task-saturation {:j1 {:t1 1}}
               :peer-sites {:p1 {:aeron/external-addr :a}
                            :p2 {:aeron/external-addr :a}
                            :p3 {:aeron/external-addr :a}
                            :p4 {:aeron/external-addr :b}
                            :p5 {:aeron/external-addr :b}
                            :p6 {:aeron/external-addr :b}}})]
       (reconfigure-cluster-workload r r))))))
