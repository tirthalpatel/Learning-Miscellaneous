To run java programs, it expects to create "course" database having "hello" collection in MongoDB as prerequisite.

Execute below commands on MongoDB Shell

	> use course
	> db.hello.insert( { name : "Hello" } )
