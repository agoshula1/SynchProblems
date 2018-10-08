'''
 Based on a solution to Readers-writers (without starvation) problem given
 in the paper "Faster Fair Solution for the Reader-Writer Problem"
 by Vlad Popov and Oleg Mazonka: https://arxiv.org/ftp/arxiv/papers/1309/1309.4507.pdf.
 As in the paper, this solution uses semaphores as the main concurrency mechanism.
 Though it shares this characteristic with the other R-W solution (see RWLBSSemaphore.java),
 it follows a different scheme than the solution from the "Little Book of Semaphores".
'''
import threading
import time

#classes to hold states of shared data
class StrData():
    def __init__(self):
        self.s = ""
    def concat(self, str):
        self.s += str
    def gets(self):
        return self.s

class SyncCtrl():
    def __init__(self):
        self.ctrin = 0
        self.ctrout = 0
        self.wait = False
        self.in_sem = threading.Semaphore(1)
        self.out = threading.Semaphore(1)
        self.wrt = threading.Semaphore(0)

# writer thread
def writer(ctrl, data, n):
    t0 = time.clock()
    ctrl.in_sem.acquire()
    ctrl.out.acquire()
    if ctrl.ctrin == ctrl.ctrout:
        ctrl.out.release()
    else:
        ctrl.wait = True
        ctrl.out.release()
        ctrl.wrt.acquire()
        ctrl.wait = False
    waitTime = time.clock() - t0

    #critical section
    item = "item" + str(n)
    data.concat(item)
    #print "\tWriting: " + item + "\n"
    #print "\tWriter wait time (sec): {}".format(waitTime)

    ctrl.in_sem.release()

#reader thread
def reader(ctrl, data):
    ctrl.in_sem.acquire()
    ctrl.ctrin += 1
    ctrl.in_sem.release()

    #critical section
    #print "Reading: " + data.gets() + "\n"

    ctrl.out.acquire()
    ctrl.ctrout += 1
    if ctrl.wait and ctrl.ctrin == ctrl.ctrout:
        ctrl.wrt.release()
    ctrl.out.release()

def test(nthreads, inc, ctrl, sharedData):
    threads = []

    for n in range(nthreads):
        if n % inc == 0:
            t = threading.Thread(target=writer, args=(ctrl, sharedstr, n))
        else:
            t = threading.Thread(target=reader, args=(ctrl, sharedstr))
        threads.append(t)
        t.start()
    #wait until threads are done
    for i in range(len(threads)):
        threads[i].join()

#correctness testing
# setup semaphores and other variables
ctrl = SyncCtrl()
sharedstr = StrData()
'''#test 1: launch readers and writers back-to-back
print "Test 1:\n"
test(20,2,ctrl,sharedstr)

ctrl = SyncCtrl()
sharedstr = StrData()
#test 2: launch several readers, with the occasional reader (to detect starvation)
test(20,10,ctrl,sharedstr)'''

#performance testing
t0 = time.clock()
test(20,2,ctrl,sharedstr);
print "Step 1: Time elapsed (sec) = {}".format(time.clock() - t0)

ctrl = SyncCtrl()
sharedstr = StrData()
t0 = time.clock()
test(200,2,ctrl,sharedstr);
print "Step 2: Time elapsed (sec) = {}".format(time.clock() - t0)

ctrl = SyncCtrl()
sharedstr = StrData()
t0 = time.clock()
test(2000,2,ctrl,sharedstr);
print "Step 3: Time elapsed (sec) = {}".format(time.clock() - t0)
