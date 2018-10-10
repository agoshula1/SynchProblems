'''
 Based on a solution to Barbershop problem given in the
 "Little Book of Semaphores" by Allen B. Downey.
 As in the book, this solution uses semaphores as the main concurrency mechanism.
'''
import threading
import time

class SharedData():
    def __init__(self):
        self.chairs = 4
        self.customers = 0
        self.stopBarber = False

def customer(data,n):
    mutex.acquire()
    if data.customers == data.chairs or data.stopBarber:
        #print "Customer " + str(n) + " rejected\n"
        mutex.release()
        return #leave the barbershop
    data.customers += 1
    mutex.release()

    #rendezvous
    cust.release()
    barb.acquire()

    #get haircut
    #print "Customer " + str(n) + " getting hair cut\n"

    #rendezvous
    customerDone.release()
    barberDone.acquire()

    mutex.acquire()
    data.customers -= 1
    mutex.release()

def lastCustomer(data,n):
    #print "last customer"
    mutex.acquire()
    if data.customers == data.chairs:
        data.stopBarber = True #premptively stop barber
        #print "Customer " + str(n) + " rejected\n"
        mutex.release()
        return #leave the barbershop
    data.customers += 1
    mutex.release()

    #rendezvous
    cust.release()
    barb.acquire()

    #get haircut
    #print "Customer " + str(n) + " getting hair cut\n"

    #rendezvous
    customerDone.release()
    barberDone.acquire()

    mutex.acquire()
    data.customers -= 1
    data.stopBarber = True #premptively stop barber
    mutex.release()

#runs indefinitely unless forced to stop
def barber(data):
    while(True):
        #rendezvous
        time.sleep(0.01) #give last customer time to communicate finish
        if data.stopBarber:
            break
        cust.acquire()
        barb.release()

        #cut hair
        #print "Barber cutting hair\n"

        #rendezvous
        customerDone.acquire()
        barberDone.release()

def simulate(numCusts,data):
    threads = []
    r = threading.Thread(target=barber, args=(data,))
    r.start()

    for n in range(numCusts - 1):
        t = threading.Thread(target=customer, args=(data,n))
        threads.append(t)
        t.start()
        #time.sleep(0.01)

    for i in range(len(threads)):
        threads[i].join()
        if threads[i].isAlive():
            print "Customer " + str(i) + " still running"

    #last customer, may stop barber before all seated customers are given haircut
    t = threading.Thread(target=lastCustomer, args=(data,numCusts - 1))
    t.start()
    t.join()

    #print "customers done"
    r.join()
    #print "barber done"

shareddata = SharedData()
mutex = threading.Semaphore(1)
cust = threading.Semaphore(0)
barb = threading.Semaphore(0)
customerDone = threading.Semaphore (0)
barberDone = threading.Semaphore (0)

#correctness testing
#simulate(30,shareddata)

#performance testing
t0 = time.clock()
simulate(20,shareddata);
print "Step 1: Time elapsed (sec) = {}".format(time.clock() - t0)

shareddata = SharedData()
t0 = time.clock()
simulate(200,shareddata);
print "Step 2: Time elapsed (sec) = {}".format(time.clock() - t0)

shareddata = SharedData()
t0 = time.clock()
simulate(2000,shareddata);
print "Step 3: Time elapsed (sec) = {}".format(time.clock() - t0)
