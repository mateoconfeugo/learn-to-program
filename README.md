# learning-to-program

[ ![Codeship Status for mateoconfeugo/learn-clojure](https://codeship.io/projects/56fd2f90-0dd3-0132-184d-0ac0b7fe3cd9/status)](https://codeship.io/projects/32332)

A Clojure web application designed to help you:
1) Learn clojure 
2) Become familar with the common libraries with an emphasis on web development
3) Learn the development deployment and maintenance tools, procedures available to make your life simplier 
4) Be able to come back to a project weeks,months,years later and have something you can run use and understand

The idea is that making easy to contribute to the project, 
test the project and deploy the project right at the beginning will help you to complete the project.
Getting a project done is about keeping the momentuem going.

Coupling awesome technology like clojure/script together with some devops best practices can help you get the project completed and allows it to be improved upon in a maintainable fashion

Every project should have out of the box
* Working Demo in the cloud
* Documentation of the code in the literate style
* Pallet deployment
* Publish artifact to repo
* Test framework
* Source Control Strategy

## Usage

Try to use lein a central point for doing all you development/operations/maintence tasks for example:

Devops:

### Launch the application in a docker container
lein pallet up -P docker
### Push the application jar/uberjar to a private s3 bucket
lein deploy private
### Launch the application to prd/stg/qa aws/rs/docker
lein pallet up prd/stg/qa -P aws/rs/docker

##  Development

### Continuous run the test suite
lein autoexpect
### Run the clojurescript compiler in develoment
lein cljsbuild auto
### Run the test suite
lein test
### Create the documentation
lein marg

### Using an iteractive environment
Being able to write the code in the application as it is running.
Creating the code while creating the test.  Then copying the test into a test suite file and evaluating that.
Further since the lein autoexpect plugin is continuous running your test it will pick up the change and run it.


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
