
// Immediately Invoked Function Expression (IIFE)
// consitute of a semicolon + a bracket pair covers an anonymous function + another bracket pair (optional to put parameters) to execute it immediately
// which can escape the variables and functions defined inside influence the outter area

;(function() { // Before ES6
    // expression      
})()

;(() => { // After ES6 (arrow method style)
    // expression
    var varInIIFE = `I'm a var in Immediately Invoked Function Expression`
    
    function funInIIFE(input){
        console.log(`${input}.\nMe and functions inside of the group oerator() can't be seen out of the area`)
    }
    
    funInIIFE(varInIIFE)
})()

// promise
{
    //funInIIFE(varInIIFE) varInIIFE and funInIIFE can't be seen out of IIFE

    let me = 'Eric'   
    let callName = function (name) { 
        console.log(`呼叫 ${name}`) 
    }
    callName(me);
    // function callName(name) { 
    //     console.log(`呼叫 ${name}`) 
    // }
    
    let x = 'I will be defined' 

    function fn1(){
        return new Promise((res, rej) => {
            setTimeout(() => {
                console.log('fn1 completed')
                res()
            }, 3000);
        })
    }

    let y = 'I will not be defined' 
    

    function fn2(){
        return new Promise((res, rej) => {
            setTimeout(() => {
                console.log('fn2 completed')
                res()
            }, 1000);
        })
    }

    function fn3(){
        return new Promise((res, rej) => {
            setTimeout(() => {
                console.log('fn3 completed')
                res()
            }, 2000);
        })
    }

    Promise.all([fn1(), fn2(), fn3()]).then(
        result => console.log('all done'), 
        err => console.log('error')
    )
}

// async, await
// if calling a async method with return value and without await, the return value will be wrapped in a Promise object
;(async () => {
    // replaced by IIFE
    // async function run (){
    //     console.log(await animalRacing('Turtle', 20))
    //     console.log(await animalRacing('Rabbit', 40))
    // }
    // run()

    console.log(`Demo: async-await / promise`)

    // async version
    async function animalRacing (speces, rate){
        let result
        let time = 1 / rate
        
        // setTimeout(() => {
            try {
                result = `${speces} finish the competition! Spent ${time} seconds!`
            } catch (error) {
                result = error
            } finally {
                return result
            }
        // }, time * 10000);
    }

    // promise version
    // function animalRacing (speces, rate){
    //     let result
    //     let time = 1 / rate
        
    //     return new Promise((resolve, reject) =>{
    //         setTimeout(() => {
    //             try {
    //                 result = `${speces} finish the competition! Spent ${time} seconds!`
    //                 resolve(result)
    //             } catch (error) {
    //                 reject(error)
    //             }
    //         }, time * 10000);
    //     })
    // }

    console.log(await animalRacing('Turtle', 20))
    console.log(await animalRacing('Rabbit', 40))
})()

