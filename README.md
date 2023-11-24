## NameService

### Use for showing possibilities to slim down Java docker images:

Build a docker image with a JDK and an Ubuntu base image:
```docker build . -t nameservice1:latest -f Dockerfile_Ubuntu_JDK```

Build a docker image with a JRE and an Ubuntu base image:
```docker build . -t nameservice2:latest -f Dockerfile_Ubuntu_JRE```

Build a docker image with a JLink created JRE and an Alpine Linux image:
```docker build . -t nameservice3:latest -f Dockerfile_Alpine_JLink```



### Use for showing CRaC (Coordinated Restore at Checkpoint)
#### Description
The nameservice is using SpringBoot to offer a REST endpoint that returns a given number
of random names for either girls, boys or both.

The endpoint can be called as follows:

http://localhost:8080/names/?gender=boy&amount=5

The expected result for this call will look like this:
```json
{ "names":[{"name":"Abe","gender":"male"},{"name":"Baxter","gender":"male"},{"name":"Dylan","gender":"male"},{"name":"Lafayette","gender":"male"},{"name":"Marlin","gender":"male" ],"response_time":"701 ms"}

```

The response will also tell you how long it took to respond to the request. 
Because all names will be loaded the first time you request the service, it will
take longer for the first call.

We added a dependency to the ```org.crac``` library that makes it possible to code
against the CRaC api without the need for a JVM that supports CRaC. With this your
code can make use of the Resource interface and in case you run the application on a 
CRaC enabled JVM, the CRaC related code will be called.

In this demo we will load all names in the ```beforeCheckpoint()``` method which means
we do not even have to use the service but at the time of a checkpoint all names will 
be loaded in the allNames list. That means after a restore all names already have been
loaded and we can directly start at full speed.

<b>ATTENTION: THIS IS A DEMO!!! I know that you usually don't do this but it helps to understand.</b>


The nameservice needs to load around 258 000 names from a json file the first time the service is called. 
The service provides methods to create a number of random names for boys, girls or both. 

#### Build the jar file on the target platform:
To create the jar file you need to build it on the target platform (Linux x64 or aarch64) using
a JDK that supports CRaC. You can find builds here [Azul](https://www.azul.com/downloads/?version=java-17-lts&os=linux&package=jdk-crac#zulu).
1. make sure you set JAVA_HOME to the JVM with CRaC support
2. go the project folder
3. run ```gradlew clean build```
4. Now you should find the the jar at ```build/libs/nameservice-17.0.0.jar```
5. This jar file will later be used to run on the docker container
6. Make sure to select the correct JDK in the docker file (x64 or aarch64)


#### Login into docker:
```docker login```


#### Build docker image:
```docker build -t nameservice -f Dockerfile_CRaC .```


#### Commit image to dockerhub (optional):
e.g.
```docker commit container_id hansolo/nameservice:latest```
```docker push hansolo/nameservice:latest```


### Use CRaC with a checkpoint in the docker container

#### 1. Start the application in a docker container
1. Open a shell window
2. Run ``` docker run -it --privileged --rm -p 8080:8080 --name nameservice nameservice ```
3. In the docker container run ```java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/nameservice-17.0.0.jar```

</br>

#### 2. Start a 2nd shell window and create the checkpoint
1. Open a second shell window
2. Run ```docker exec -it -u root nameservice /bin/bash```
3. Enable checkpoint compression ```export CRAC_CRIU_OPTS=--compress```
4. Execute ``` top ``` command and note the PID of the running java process
5. Take the PID and run ``` jcmd PID JDK.checkpoint```
6. In the first shell window the application should have created the checkpoint
7. Check the folder /opt/crac-files for the checkpoint files being present
8. In second shell window run ``` exit ``` to get back to your machine

</br>

#### 3. Commit the current state of the docker container
1. Now get the CONTAINER_ID from shell window 1 by execute ``` docker ps -a ``` in shell window 2
2. Run ``` docker commit CONTAINER_ID nameservice:checkpoint ``` in shell window 2
3. Exit the docker container in shell window 1 by executing ``` exit ```

</br>

#### 4. Run the docker container from the saved state incl. the checkpoint
Run docker image without checkpoint:
```docker run -it --privileged --rm -p 8080:8080 --name nameservice nameservice:checkpoint java -jar /opt/app/nameservice-17.0.0.jar```

Run docker image with checkpoint:
```docker run -it --privileged --rm -p 8080:8080 --name nameservice nameservice:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files```

</br>

#### 5. Create a shell script to restore multiple times
1. Open a shell window
2. Create a text file named ```restore_nameservice.sh```
3. Add
```
#!/bin/bash

echo "docker run -it --privileged --rm -p 8080:8080 --name $1 nameservice:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files"

docker run -it --privileged --rm -p 8080:8080 --name $1 nameservice:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files
```
4. Make the script executable by executing ```chmod +x restore_nameservice.sh```
5. Now you can start the docker container multiple times executing ```restore_nameservice.sh NAME_OF_CONTAINER```

If you would like to start the original container without the checkpoint you can still
do that by executing the following command
```docker run -it --privileged --rm -p 8080:8080 --name nameservice nameservice:checkpoint java -jar /opt/app/nameservice-17.0.0.jar```


### Use CRaC with a checkpoint in an external volume

#### 1. Create and use a docker volume
1. Create a folder on the machine you run the docker image on e.g. ```mkdir /home/hansolo/docker_volume```
2. Create a docker volume:
```docker volume create --driver local --opt type=none --opt device=/home/hansolo/docker_volume --opt o=bind myvolume```
3. Run the docker using the volume:
```docker run -it --privileged --rm -v myvolume:/checkpoints -p 8080:8080 --name nameservice nameservice```
4. In the docker container enable checkpoint compression
```export CRAC_CRIU_OPTS=--compress```
5. In the docker container run 
```java -XX:CRaCCheckpointTo=/checkpoints -jar /opt/app/nameservice-17.0.0.jar```


#### 2. Start a 2nd shell window and create the checkpoint
1. Open a second shell window
2. Run ```docker exec -it -u root nameservice /bin/bash```
4. Execute ``` top ``` command and note the PID of the running java process
5. Take the PID and run ``` jcmd PID JDK.checkpoint```
6. In the first shell window the application should have created the checkpoint
7. Check the folder /checkpoints for the checkpoint files being present
8. In second shell window run ``` exit ``` to get back to your machine
   

#### 3. Exit 1st shell window
1. In first shell window run ```exit```to get back to your machine
2. The checkpoint should now be in the folder you created e.g. /home/hansolo/docker_volume


#### 4. Create and use a docker volume
1. Create folder on the target machine e.g.: ```mkdir /Users/hansolo/docker_volume```
2. Copy the files from the former created checkpoint to this folder
3. Create a docker volume on the target machine:
 ```docker volume create --driver local --opt type=none --opt device=/Users/hansolo/docker_volume --opt o=bind myvolume```


#### 5. Run the docker image with the checkpoint on the external volume
1. Run the docker image ```docker run -it --privileged --rm -v myvolume:/checkpoints -p 8080:8080 --name nameservice nameservice:checkpoint java -XX:CRaCRestoreFrom=/checkpoints```  
