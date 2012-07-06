/*
   Determine the lowest common multiple of a series of integers

   Last modified: 05 Feb 2011
          Author: Derek Battams <derek AT battams DOT ca>
*/

def nums = [32, 84, 821, 33] // This is the list of integers to calculate the LCM of; YOU MUST USE POSITIVE INTEGERS ONLY!

print "LCM of ${nums} is "
println lcmm(nums).toString()

// Find LCM of an arbitrary list of numbers
def lcmm(List args) {
    if(args.size() == 2)
        return lcm(args[0], args[1])
    else {
        def arg0 = args.pop()
        return lcm(arg0, lcmm(args))
    }
}

//Determine lowest common multiple of two ints
def lcm(a, b) {
 return (a * b).intdiv(gcd(a, b))
}

//Determine greatest common divisor of two ints
def gcd(a, b) {
 // Euclidean algorithm
 while (b != 0) {
     t = b
     b = a % b
     a = t
 }
 return a
}