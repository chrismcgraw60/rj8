# rj8
Junit visualisation (parked) 

- Ingests Junit results hitting the file system and structures them for persistence in RDBMS. File parsing is streamed to DB input layer via RxJava. 
- Enables reporting of results via Play Framework app. Query results are streamed out via RxJava down through websockets. 
- Results consumed from an Angular app using D3 for presentation. 
