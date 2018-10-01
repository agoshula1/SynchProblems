import threading

class StrData():
    def __init__(self):
        self.s = ""
    def concat(self, str):
        self.s += str
    def gets(self):
        return self.s

# writer thread
def writer(in_sem, out, wrt, ctrin, ctrout, wait, data, n):
    in_sem.acquire()
    out.acquire()
    if(ctrin == ctrout):
        out.release()
    else:
        wait = True
        out.release()
        wrt.acquire()
        wait = False

    #critical section
    # Do some work
    item = "item" + str(n)
    data.concat(item)
    print "Writing: " + item + "\n"

    in_sem.release()

#reader thread
def reader(in_sem, out, wrt, ctrin, ctrout, wait, data):
    in_sem.acquire()
    ctrin += 1
    in_sem.release()

    #critical section
    print "Reading: " + data.gets() + "\n"

    out.acquire()
    ctrout += 1
    if(wait and ctrin == ctrout):
        wrt.release()
    out.release()

# setup semaphores and other variables
in_sem = threading.Semaphore(1)
out = threading.Semaphore(1)
wrt = threading.Semaphore(0)
ctrin = 0
ctrout = 0
wait = False
nthreads = 50
shareddata = StrData()
for n in range(nthreads/2):
    t = threading.Thread(target=writer, args=(in_sem, out, wrt, ctrin, ctrout, wait, shareddata, n))
    r = threading.Thread(target=reader, args=(in_sem, out, wrt, ctrin, ctrout, wait, shareddata))
    t.start()
    r.start()
