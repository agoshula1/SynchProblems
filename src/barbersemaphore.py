'''
 Based on a solution to Barbershop problem given in the
 "Little Book of Semaphores" by Allen B. Downey.
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading

class SharedData():
    def __init__(self):
        self.chairs = 4
        self.customers = 0

def customer(data,n):
    mutex.acquire()
    if data.customers == data.chairs:
        mutex.release()
        return #leave the barbershop
    data.customers += 1
    mutex.release()

    #rendezvous
    cust.release()
    barb.acquire()

    #get haircut
    print "Customer " + str(n)

    #rendezvous
    customerDone.release()
    barberDone.acquire()

    mutex.acquire()
    data.customers -= 1
    mutex.release()

def barber(n):
    #rendezvous
    cust.acquire()
    barb.release()

    #cut hair
    print "Barber " + str(n)

    #rendezvous
    customerDone.acquire()
    barberDone.release()

shareddata = SharedData()
mutex = threading.Semaphore(1)
cust = threading.Semaphore(0)
barb = threading.Semaphore(0)
customerDone = threading.Semaphore (0)
barberDone = threading.Semaphore (0)
ncustthreads = 10

#need to find how to keep a barber around while customers are present
threads = []
for n in range(ncustthreads):
    t = threading.Thread(target=customer, args=(shareddata,n))
    threads.append(t)
    t.start()
'''while(True):
    t = threading.Thread(target=barber, args=(n,))
    t.start()
    deadcnt = 0
    for n in range(ncustthreads):
        if not threads[n].isAlive():
            deadcnt += 1
    if deadcnt == ncustthreads:
        break
'''
