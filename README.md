
# 🏆 **Endpoint Explorers**

The application enables control of multi-objective evolutionary algorithms (MOEA) in a client-server architecture. The client is a console application that allows running experiments and collecting results from the server.

We are able to run experiments with specific problem names, algorithms, metrics and number of evaluations.

---

## Table of Contents

1. [Techologies used](#techologies-used)
2. [Model](#model)
3. [API Endpoints](#api-endpoints)
    - [Run Experiment](#run-experiment)
    - [Get Experiment By ID](#get-experiment-by-id)
    - [Get Ready Experiments](#get-ready-experiments)
    - [Get Experiments By Status](#get-experiments-by-status)

4. [Cli Commands](#cli-commands)
    - [`run`](#run-command)
    - [`get`](#get-command)
    - [`list`](#list-command)
    - [`exit`](#exit-command)
    - [`help`](#help-command)
5. [Running project](#running-project)
6. [Example usage](#example-usage)
7. [Authors](#authors)

---

## Techologies used

🌱 Spring Boot - server

🐘 Postgres - database

⚡ RxJava - running problems asynchronously

💻 PicoCLI - easy-to-use library for creating CLI

🔬 MOEA Framework - framework used for creating and running experiments

✂️ Lombok - automates the generation of boilerplate code like getters, 
setters, and constructors

🔗 Hibernate - handles object-relational mapping

🐳 Docker - containerization platform, used here for running Postgres

## Model


![Architecture Diagram](/docs/diagram.png "Architecture Overview")

### Experiment table

- **problem (String)**: Specifies the problem name. The complete list of problems can be found [here](https://github.com/MOEAFramework/MOEAFramework/blob/master/docs/listOfProblems.md).

- **algorithm (String)**: Specifies the algorithm name. The complete list of algorithms can be found [here](https://github.com/MOEAFramework/MOEAFramework/blob/master/docs/listOfAlgorithms.md).

- **numberOfEvaluations (Integer)**: Indicates how many iterations of the algorithm are performed. The greater the number, the longer the computations will take.

- **metricsList (List<Metrics>)**: For each experiment, we store multiple metrics. Metrics are recorded every 100th iteration. As a result, the total number of rows in the metrics table for a given experiment is calculated as:  
  **(Number of metrics provided × Number of evaluations) / 100**

- **status (Enum)**: Specifies the status of an experiment. The possible statuses are:  
  - **IN_PROGRESS**: The experiment is still running.  
  - **FAILED**: The experiment has failed.  
  - **READY**: The experiment is complete; all computations are done, but the CLI client has not yet been informed.  
  - **COMPLETED**: The experiment is finished, and the CLI client has been notified.

<span style="color: red;">**Important:**</span> CLI periodically sends requests to the endpoint `{BASE_URL}/experiment/ready`.  
If there is an experiment with status **READY**, the CLI is informed, and the status is changed to **COMPLETED** on the server side.


### Metrics table

- **metricsName (String)**: Specifies the name of the metric.

- **experiment (Experiment)**: Associates the metric with a specific experiment.

- **iterationNumber (Integer)**: Indicates the iteration during which the metric was computed. <span style="color: red;">Metrics are recorded every 100th iteration<span>

- **value (Float)**: Represents the value of the metric.


## API Endpoints

### Run Experiment
- **URL**: `/experiment`
- **Method**: `POST`
- **Description**: Starts a new experiment based on the provided configuration.
- **Request Body**: 
  - Valid `RunExperimentRequest` object containing:
    - `problemName` (String): Name of the problem to solve.
    - `algorithm` (String): Algorithm to use for solving the problem.
    - `numberOfEvaluations` (Integer): Number of evaluations for the experiment.


---

### Get Experiment By ID
- **URL**: `/experiment/{id}`
- **Method**: `GET`
- **Description**: Fetches details of a specific experiment by its ID.
- **Path Parameter**:
  - `id` (Integer): The ID of the experiment.

---

### Get Ready Experiments
- **URL**: `/experiment/ready`
- **Method**: `GET`
- **Description**: Retrieves a list of experiments that are marked as **READY** (completed computations, but not yet acknowledged by the CLI).

---

### Get Experiments By Status
- **URL**: `/experiment/list/{status}`
- **Method**: `GET`
- **Description**: Retrieves a list of experiments filtered by their status.
- **Path Parameter**:
  - `status` (String): The status of the experiments to filter by. Valid statuses include `IN_PROGRESS`, `READY`, `FAILED`, and `COMPLETED`.

---



## Cli Commands

### `run` command
**Description**: Run an experiment on the server.

#### Usage
```bash
run <problemName> <algorithm> [options]
```

#### Parameters

- `<problemName>`: The name of the problem to solve.
- `<algorithm>`: The algorithm to use for solving the problem.

#### Options

- `-m, --metrics <metric1> <metric2> ...`: List of metrics to evaluate (default: `all`).
- `-e, --evaluations <number>`: The number of evaluations to perform (default: `1000`).


---

### `get` command
**Description**: Retrieve details of a specific experiment from the server.

#### Usage
```bash
get <experimentId>
```

#### Parameters

- `<experimentId>`: The id of the experiment.

---


### `list` command
**Description**: Retrieve information about experiments filtered by their status.

#### Usage
```bash
list [experimentStatus]
```
We can also list all experiments info

```bash
list all 
```
#### Parameters

- `[experimentStatus]`: the status of the experiments to filter by. Defaults to all if not specified. Valid statuses include:
**IN_PROGRESS**
**READY**
**FAILED**
**COMPLETED**

---

### `exit` command
**Description**: Exits the CLI application.

#### Usage
```bash
exit
```
---

### `help` command
**Description**: Displays a list of available commands with descriptions or provides details for a specific command.

#### Usage
```bash
help [command]
```
or we can list available commands:

```bash
help
```
#### Parameters

- `[command]`: one of the avaiable commands in application for example `run` or `get`



---





## Running Project
### Prerequisites

1. **Clone the Project**  
   Clone the repository to your local machine.

2. **Java 21**  
   Ensure you have Java 21 installed (the project is developed with Java 21).

3. **Docker Engine**  
   Ensure Docker Engine is running in the background. It is necessary for setting up the PostgreSQL database.


### Setup Instructions

1. **Run `docker-compose.yml`**  
   Execute the `docker-compose.yml` file to set up the database.  

   - The credentials for connecting to the database are included in the `docker-compose.yml` file.
   - Pay attention to the `POSTGRES_DB: database` setting for the database name.

2. **Start the Server**  
   Run the `EndpointExplorersApplication` class to start the server.

3. **Run the CLI Application**  
   Open a second terminal and run the `InteractiveApp` class to launch the CLI.


##### Everything Should Work!

---

## Example Usage 

### **To run the UF1 problem with the eMOEA algorithm using `GenerationalDistance` as the metrics and 10,000 evaluations:**

```bash
run UF1 eMOEA -m generational-distance -e 10000
```

### **To run the UF1 problem with the eMOEA algorithm using default metrics and evaluations:**

```bash
run UF1 eMOEA -m generational-distance -e 10000
```


## Authors 

- **Wiktor Dybalski**  
- **Tomasz Furgała**  
- **Piotr Śmiałek**  