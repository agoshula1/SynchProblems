/*
 Based on a solution to River crossing problem given in a blog post by
 Marcelo Magallon: https://blog.ksub.org/bytes/2016/03/20/river-crossing-problem/
 This solution uses channels and select statements as the main concurrency mechanisms.
*/
package main

import (
    "fmt"
    //"time"
)

type Hacker struct{
  id int
}
type Serf struct{
  id int
}

func main(){
  hackerChan := make(chan Hacker)
  serfChan := make(chan Serf)

  //produce hackers
  go func(){
    i := 0
    for{
      hackerChan <- Hacker{i}
      i++
    }
  }()

  //produce serfs
  go func(){
    i := 0
    for{
      serfChan <- Serf{i}
      i++
    }
  }()

  //manage boat
  hackers := 0
  serfs := 0
  var hTemp chan Hacker
  var sTemp chan Serf

  for{
    select{
    case <- hackerChan:
      hackers++
    case <- serfChan:
      serfs++
    }

    if (hackers + serfs) == 3{
      switch {
    		case hackers == 2 || serfs == 3:
    			hTemp = hackerChan
          hackerChan = nil
    		case hackers == 3 || serfs == 2:
    			sTemp = serfChan
          serfChan = nil
    	}
    }else if (hackers + serfs) == 4{
      //launch boat
      fmt.Printf("hackers: %d serfs: %d\n", hackers, serfs)
      hackers = 0
      serfs = 0
      if hackerChan == nil{
        hackerChan = hTemp
      }else if serfChan == nil{
        serfChan = sTemp
      }
    }
  }
}
