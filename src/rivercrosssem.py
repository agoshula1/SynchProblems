'''
 Based on a solution to River crossing problem given in the
 "Little Book of Semaphores" by Allen B. Downey.
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading
import time

class Barrier:
    def __init__(self, n):
        self.n = n
        self.count = 0
        self.mutex = threading.Semaphore(1)
        self.turnstile = threading.Semaphore(0)
        self.turnstile2 = threading.Semaphore(0)

    def phase1(self):
        self.mutex.acquire()
        self.count += 1
        if self.count == self.n:
            for n in range(self.n):
                self.turnstile.release()
        self.mutex.release()
        self.turnstile.acquire()

    def phase2(self):
        self.mutex.acquire()
        self.count -= 1
        if self.count == 0:
            for n in range(self.n):
                self.turnstile2.release()
        self.mutex.release()
        self.turnstile2.acquire()

    def wait(self):
        self.phase1()
        self.phase2()

class SharedData():
    def __init__(self):
        self.hackers = 0
        self.serfs = 0

def hacker(barr,data):
    isCaptain = False
    mutex.acquire()
    data.hackers += 1
    if data.hackers == 4:
        for n in range(4):
            hackerQueue.release()
        data.hackers = 0
        #print "hackers: 4 serfs: 0"
        isCaptain = True
    elif data.hackers == 2 and data.serfs >= 2:
        for n in range(2):
            hackerQueue.release()
            serfQueue.release()
        data.serfs -= 2
        data.hackers = 0
        #print "hackers: 2 serfs: 2"
        isCaptain = True
    else:
        mutex.release()

    hackerQueue.acquire()

    #board
    barr.wait()

    if isCaptain:
        #row boat
        mutex.release()

def serf(barr,data):
    isCaptain = False
    mutex.acquire()
    data.serfs += 1
    if data.serfs == 4:
        for n in range(4):
            serfQueue.release()
        data.serfs = 0
        #print "hackers: 0 serfs: 4"
        isCaptain = True
    elif data.serfs == 2 and data.hackers >= 2:
        for n in range(2):
            serfQueue.release()
            hackerQueue.release()
        data.hackers -= 2
        data.serfs = 0
        #print "hackers: 2 serfs: 2"
        isCaptain = True
    else:
        mutex.release()

    serfQueue.acquire()

    #board
    barr.wait()

    if isCaptain:
        #row boat
        mutex.release()

def simulate(numProg, barr, data):
    threads = []
    for n in range(numProg):
        if n % 2 == 0:
            t = threading.Thread(target=hacker, args=(barr,data))
        else:
            t = threading.Thread(target=serf, args=(barr,data))
        threads.append(t)
        t.start()
    for i in range(len(threads)):
        threads[i].join()

barrier = Barrier(4)
mutex = threading.Semaphore(1)
shareddata = SharedData()
hackerQueue = threading.Semaphore(0)
serfQueue = threading.Semaphore(0)

#correctness testing
#simulate(60,barrier,shareddata)

#performance testing
t0 = time.clock()
simulate(40,barrier,shareddata);
print "Step 1: Time elapsed (sec) = {}".format(time.clock() - t0)

barrier = Barrier(4)
shareddata = SharedData()
t0 = time.clock()
simulate(400,barrier,shareddata);
print "Step 2: Time elapsed (sec) = {}".format(time.clock() - t0)

barrier = Barrier(4)
shareddata = SharedData()
t0 = time.clock()
simulate(4000,barrier,shareddata);
print "Step 3: Time elapsed (sec) = {}".format(time.clock() - t0)
