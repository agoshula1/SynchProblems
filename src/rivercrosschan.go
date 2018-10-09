/*
 Based on a solution to River crossing problem given in a blog post by
 Marcelo Magallon: https://blog.ksub.org/bytes/2016/03/20/river-crossing-problem/
 This solution uses channels and select statements as the main concurrency mechanisms.
*/
package main

import (
    "sync"
    "fmt"
    "time"
)

type Hacker struct{
  id int
}
type Serf struct{
  id int
}

func prodHackers(num int, h chan Hacker){
  for i := 0; i < num; i++{
    h <- Hacker{i}
  }
}

func prodSerfs(num int, s chan Serf){
  for i := 0; i < num; i++{
    s <- Serf{i}
  }
}

func simulate(numProgrammers int, h chan Hacker, s chan Serf){
  //produce hackers
  go prodHackers(numProgrammers/2, h)

  //produce serfs
  go prodSerfs(numProgrammers/2, s)

  //manage boat
  hackers := 0
  serfs := 0
  var hTemp chan Hacker
  var sTemp chan Serf

  for i := 0; i < numProgrammers; i++{
    select{
    case <- h:
      hackers++
    case <- s:
      serfs++
    }

    if (hackers + serfs) == 3{
      switch {
    		case hackers == 2 || serfs == 3:
    			hTemp = h
          h = nil
    		case hackers == 3 || serfs == 2:
    			sTemp = s
          s = nil
    	}
    }else if (hackers + serfs) == 4{
      //row boat
      //fmt.Printf("hackers: %d serfs: %d\n", hackers, serfs)
      hackers = 0
      serfs = 0
      if h == nil{
        h = hTemp
      }else if s == nil{
        s = sTemp
      }
    }
  }
}

func main(){
  hackerChan := make(chan Hacker)
  serfChan := make(chan Serf)
  //correctness testing
  //simulate(60, hackerChan, serfChan)

  //performance testing
  start := time.Now()
  simulate(40, hackerChan, serfChan)
  end := time.Now()
  difference := end.Sub(start)
  fmt.Printf("Step 1: Time elapsed (sec) = %f\n", difference.Seconds())

  //reinitialize channels
  hackerChan = make(chan Hacker)
  serfChan = make(chan Serf)
  start = time.Now()
  simulate(400, hackerChan, serfChan)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 2: Time elapsed (sec) = %f\n", difference.Seconds())

  //reinitialize channels
  hackerChan = make(chan Hacker)
  serfChan = make(chan Serf)
  start = time.Now()
  simulate(4000, hackerChan, serfChan)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 3: Time elapsed (sec) = %f\n", difference.Seconds())
}
