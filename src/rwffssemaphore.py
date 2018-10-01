'''
 Based on a solution to Readers-writers (without starvation) problem given
 in the paper "Faster Fair Solution for the Reader-Writer Problem"
 by Vlad Popov and Oleg Mazonka: https://arxiv.org/ftp/arxiv/papers/1309/1309.4507.pdf.
 As in the paper, this solution uses semaphores as the main concurrency mechanism.
 Though it shares this characteristic with the other R-W solution (see RWLBSSemaphore.java),
 it follows a different scheme than the solution from the "Little Book of Semaphores".
'''
import threading

#classes to hold states of shared data
class StrData():
    def __init__(self):
        self.s = ""
    def concat(self, str):
        self.s += str
    def gets(self):
        return self.s

class LogicalData():
    def __init__(self):
        self.ctrin = 0
        self.ctrout = 0
        self.wait = False

# writer thread
def writer(in_sem, out, wrt, ctrl, data, n):
    in_sem.acquire()
    out.acquire()
    if ctrl.ctrin == ctrl.ctrout:
        out.release()
    else:
        ctrl.wait = True
        out.release()
        wrt.acquire()
        ctrl.wait = False

    #critical section
    # Do some work
    item = "item" + str(n)
    data.concat(item)
    print "Writing: " + item + "\n"

    in_sem.release()

#reader thread
def reader(in_sem, out, wrt, ctrl, data):
    in_sem.acquire()
    ctrl.ctrin += 1
    in_sem.release()

    #critical section
    print "Reading: " + data.gets() + "\n"

    out.acquire()
    ctrl.ctrout += 1
    if ctrl.wait and ctrl.ctrin == ctrl.ctrout:
        wrt.release()
    out.release()

# setup semaphores and other variables
in_sem = threading.Semaphore(1)
out = threading.Semaphore(1)
wrt = threading.Semaphore(0)
ctrl = LogicalData()
nthreads = 50
sharedstr = StrData()
for n in range(nthreads/2):
    t = threading.Thread(target=writer, args=(in_sem, out, wrt, ctrl, sharedstr, n))
    r = threading.Thread(target=reader, args=(in_sem, out, wrt, ctrl, sharedstr))
    t.start()
    r.start()
