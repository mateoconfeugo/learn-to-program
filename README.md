# learning-to-program

[ ![Codeship Status for mateoconfeugo/learn-clojure](https://codeship.io/projects/56fd2f90-0dd3-0132-184d-0ac0b7fe3cd9/status)](https://codeship.io/projects/32332)

A Clojure web application designed to help you:
1) learn clojure 
2) become familar with the common libraries with an emphasis on web development
3) learn the development deployment and maintenance tools, procedures available to make your life simplier 

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
