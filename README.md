# Evolutionary Experiment Platform

The **Evolutionary Experiment Platform** is a client-server application that facilitates the execution and management of multi-objective evolutionary algorithm experiments. It allows clients to interact with a server, run various optimization experiments asynchronously, and collect results using a simple console interface.

The platform leverages a client-server architecture, where:
- **Server**: Powered by Spring Boot and PostgreSQL, handles experiment execution, data storage, and retrieval.
- **Client**: A command-line interface (CLI) application that allows users to submit experiments, retrieve results, and interact with the system.

This project enables the execution of optimization experiments with customizable problem definitions, algorithms, metrics, and a specified number of evaluations. Experiments are executed asynchronously to ensure efficient processing and scalability.

---
## Features 🚀

- 🎯 **Run experiments**: Execute single or multiple experiments with specific configurations and parameters. 
- 📊 **Retrieve experiment results**: Fetch results as metrics for performance evaluation during and after the experiments. 
- 📝 **List experiments**: List all experiments with filtering options for easy tracking and retrieval. 
- 📁 **Group experiments**: Organize experiments into groups for better management and retrieval. 
- 📈 **Statistics aggregation**: Calculate statistics (such as averages, medians, and standard deviations) across multiple experiments to evaluate the overall performance. 
- 📅 **CSV export**: Generate CSV files containing statistics from experiments for further analysis and reporting. 
- 📉 **Plot generation**: Create visual plots showing metric values across evaluations, with dynamic updates as the experiment progresses. 

---

## Technologies Used 💻

This project utilizes a variety of modern technologies to create a robust and scalable system:

- 🌱 **Spring Boot** – Server-side application framework.
- 🐘 **PostgreSQL** – Database used to store experiment data.
- ⚡ **RxJava** – Asynchronous processing of optimization problems.
- 🔧 **Guice** - A lightweight dependency injection framework for managing and wiring components.
- 💻 **PicoCLI** – A library for creating the command-line interface (CLI).
- 🔬 **MOEA Framework** – A framework for creating and running multi-objective optimization experiments.
- ✂️ **Lombok** – A Java library to reduce boilerplate code for getters, setters, and constructors.
- 🔗 **Hibernate** – Object-relational mapping (ORM) framework.
- 🐳 **Docker** – Containerization platform used to run PostgreSQL in isolated environments.

---
## Getting Started ⚡

### Prerequisites 🛠

Before getting started, make sure you have the following software installed:

- **Java 21**: Required to build and run the Spring Boot application.
- **PostgreSQL**: To store and manage experiment data. Docker can be used to run it easily.
- **Docker**: A containerization platform used to run PostgreSQL in an isolated environment, ensuring consistent and simplified deployment of the database service.
- **Maven**: A build automation tool used for managing dependencies, building the application, and running tests. You’ll need Maven to build and run the project.

### Setup ⚙️

1. **Clone the repository**:
   ```bash
   git clone https://github.com/TommyFurgi/Evolutionary-Experiment-Platform
   cd Evolutionary-Experiment-Platform
   ```

2. **Run PostgreSQL using Docker**:
    Make sure you have Docker installed. To start PostgreSQL in a container, run the following command:
    ```bash
    docker-compose up
    ```
    > ℹ️ **Keep this terminal open to ensure the database is running.**

3. **Start the Spring Boot server**:
    Once PostgreSQL is running, open **another new terminal window** and start the Spring Boot server:
    ```bash
    cd .\server\
    mvn spring-boot:run
    ```
    > ℹ️ **The server must be running to process CLI commands.**


4. **Run the CLI**:
       Open a **new terminal window** to interact with the system via the command-line interface. Then, execute the following commands:  
    ```bash
    cd .\cli\
    mvn exec:java
    ```
    >  ℹ️ **The CLI is the primary interface for interacting with the system. Use it to execute experiments, retrieve results, and manage configurations.**

### Next Steps 🚀
At this point, all components of the system are running. You can now use the CLI to execute experiments, retrieve results, and perform other operations.

Here are some example commands. Try them out in the terminal with the running CLI and see the results:

- **Running a single experiment**:
    To run an example experiment with the UF1 problem and the e-MOEA algorithm, calculating all metrics:
    ```bash
    run uf1 e-moea
    ```

- **Getting metrics value**:
    After running the previous command, the experiment will have an ID (e.g., `1`). To retrieve the metrics for this experiment:
    ```bash
    get 1
    ```

- **Listing all experiments**:
    To list all experiments, use:
    ```bash
    list
    ```

For a detailed guide on available commands and how to use them, refer to the [CLI Usage Guide](./INSTRUCTION.md).

---
## Authors ✨

The project was developed by the group **Endpoint Explorers**:
- [Piotr Śmiałek](https://github.com/daredevilq)  
- [Wiktor Dybalski](https://github.com/WiktorDybalski)  
- [Tomasz Furgała](https://github.com/TommyFurgi)