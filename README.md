# 행복 Cinema (영화 예매 서비스)

![image](https://user-images.githubusercontent.com/35188271/108287851-d410bb00-71ce-11eb-85fc-25edf75d4960.png)
　  
   　  

# 서비스 시나리오

`기능적 요구사항`
1. 고객이 예약서비스 예매를 한다.
1. 고객이 입장료를 결제한다.
1. 보증금 결제가 완료되면 예약내역이 식당에 전달된다.
1. 영화관에 예약정보가 전달되면 예약서비스에 예매상태를 완료 상태로 변경한다.
1. 예약이 완료되면 예약서비스에서 현재 예약상태를 조회할 수 있다.
1. 고객이 예매를 취소할 수 있다.
1. 고객이 모든 진행내역을 볼 수 있어야 한다.
  
  
`비기능적 요구사항`
1. 트랜잭션
    1. 결재되지 않으면 예약이 안되도록 한다.(Sync)
    1. 결제를 취소하면 payment를 환불하고 영화 예약취소 내역을 전달한다.(Async)
1. 장애격리
    1. payment 시스템이 과중되면 예약을 받지 않고 잠시후에 하도록 유도한다(Circuit breaker, fallback)
    1. Cinema 서비스가 중단되더라도 예약은 받을 수 있다.(Asyncm, Event Dirven)
1. 성능
    1. 고객이 예약상황을 조회할 수 있도록 별도의 view로 구성한다.(CQRS)  


　    
  
  
# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)  

　  
  
# 분석/설계

### Event Storming 결과
![image](https://user-images.githubusercontent.com/35188271/108287881-e3900400-71ce-11eb-9c58-8ee65c54865d.png)
　     
### 기능적 요구사항 검증(1)

![image](https://user-images.githubusercontent.com/35188271/108288147-729d1c00-71cf-11eb-80a6-fc4eb058e9f4.png)

    - 고객이 영화관을 예약한다.(OK)
    - 고객이 표값을 결제한다.(OK)
    - 결제가 완료되면 예약내역이 영화관에 전달된다.(OK)
    - 영화관에 예약정보가 전달되면 예약서비스에 예약상태를 완료 상태로 변경한다.(OK)
    - 예약이 완료되면 예약서비스에서 현재 예약상태를 조회할 수 있다.(OK)
    
　  
　  
### 기능적 요구사항 검증(2)   
![image](https://user-images.githubusercontent.com/35188271/108288351-c90a5a80-71cf-11eb-811e-20a7bf9a6efa.png)
    - 고객이 예약을 취소할 수 있다.(OK)
    - 결제 취소하면 환불한다.(OK)
    - 결제상태를 payment 서비스에서 조회 할 수 있다.(OK)  
    
    
　  
　  
### 기능적 요구사항 검증(3)   
![image](https://user-images.githubusercontent.com/35188271/108288460-f6ef9f00-71cf-11eb-9a0c-919934c69e33.png)

    - 고객이 모든 진행내역을 볼 수 있어야 한다.(OK)
    
　  
　  
   
### 비기능 요구사항 검증

    - 결제서비스(payment)에서 결제되지 않으면 예약이 안되도록 해아 한다.(Req/Res)
    - Cinema 서비스가 중단되더라도 예약은 받을 수 있어야 한다.(Pub/Sub)
    - payment 시스템이 과부화 되면 예약을 받지 않고 잠시후에 하도록 유도한다(Circuit breaker)
    - 예약을 취소하면 지불한 금액을 환불하고 영화관에 예약취소 내역을 업데이트해야 한다.(SAGA)
    - 고객이 예매상황을 조회할 수 있도록 별도의 view로 구성한다.(CQRS) 
    
　  
　  
           
　  　  
   

# Polyglot

Reservation, Payment, Information는 H2로 구현하고 Cinema 서비스의 경우 Hsql로 구현하여 MSA간의 서로 다른 종류의 Database에도 문제없이 작동하여 다형성을 만족하는지 확인하였다.

- reservation, payment, information의 pom.xml 파일 설정

![image](https://user-images.githubusercontent.com/35188271/108289038-08857680-71d1-11eb-974c-5967ff0ef686.png)

    
　  
 
- cinema 서비스의 pom.xml 파일 설정

![image](https://user-images.githubusercontent.com/35188271/108288974-e7248a80-71d0-11eb-8d94-0bad847706e6.png)
    
　  
    
　  
　  
   

# Req/Resp
```
1. 분석단계에서의 조건 중 하나로 예약(reservation)-> 결제(payment) 간의 호출은 동기식 일관성을 유지하는
트랜잭션으로 처리하기로 하였다. 

2. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 
호출하도록 한다. 
```
    
　  
    
    
- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현  (paymentService.java)

![image](https://user-images.githubusercontent.com/35188271/108289141-38347e80-71d1-11eb-980e-500a77beb303.png)

    
　  
    

- 예약을 받은 직후(@PostPersist) 예치금 결제를 요청하도록 처리

![image](https://user-images.githubusercontent.com/35188271/108289283-847fbe80-71d1-11eb-9a07-90bde330304c.png)

    
　  
　  
### 동기식 결제 장애시

```
# 결제 (payment) 서비스를 잠시 내려놓음
# 예약 처리
kubectl delete deploy deposit
```

![image](https://user-images.githubusercontent.com/35188271/108289549-efc99080-71d1-11eb-9dee-c9a84f33a054.png)


- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 예치금 결제 시스템이 장애가 나면 예약도 못받는다는 것을 확인

![image](https://user-images.githubusercontent.com/35188271/108289527-e9d3af80-71d1-11eb-8f09-6bc40927e965.png)
　  
```
# 결재(payment)서비스 재기동
kubectl create deploy payment --image=skwooacr.azurecr.io/payment:latest
```
![image](https://user-images.githubusercontent.com/35188271/108289689-37501c80-71d2-11eb-80e0-09cdb4056818.png)


　  
　  
   
# Gateway
- gateway > application.yml

![image](https://user-images.githubusercontent.com/35188271/108289738-5058cd80-71d2-11eb-9dcc-272290a2e4bc.png)

　  

- Gateway의 External-IP 확인

![image](https://user-images.githubusercontent.com/35188271/108289934-b0e80a80-71d2-11eb-82e6-714ba6d50ca9.png)

    
　  
　  
- External-IP 로 Reservation서비스에 접근


![image](https://user-images.githubusercontent.com/35188271/108290127-12a87480-71d3-11eb-8858-5fd719de2a65.png)
　      
　  
　  

# Deploy

- Deploy API 호출

```
# 소스를 가져와 각각의 MSA 별로 빌드 진행

# 도커라이징 : Azure Registry에 Image Push 
az acr build --registry skwooacr --image skwooacr.azurecr.io/reservation:latest .  
az acr build --registry skwooacr --image skwooacr.azurecr.io/payment:latest . 
az acr build --registry skwooacr --image skwooacr.azurecr.io/cinema:latest .   
az acr build --registry skwooacr --image skwooacr.azurecr.io/information:latest .   
az acr build --registry skwooacr --image skwooacr.azurecr.io/gateway:latest . 

# 컨테이터라이징 : Deploy, Service 생성
kubectl create deploy reservation --image=skwooacr.azurecr.io/reservation:latest
kubectl expose deploy reservation --type="ClusterIP" --port=8080
kubectl create deploy payment --image=skwooacr.azurecr.io/payment:latest
kubectl expose deploy payment --type="ClusterIP" --port=8080 
kubectl create deploy restaurant --image=skwooacr.azurecr.io/restaurant:latest
kubectl expose deploy restaurant --type="ClusterIP" --port=8080
kubectl create deploy information --image=skwooacr.azurecr.io/information:latest 
kubectl expose deploy information --type="ClusterIP" --port=8080
kubectl create deploy gateway --image=skwooacr.azurecr.io/gateway:latest 
kubectl expose deploy gateway --type=LoadBalancer --port=8080
#kubectl get pod,service,deploy
```
    
　  
　  
- Deploy 확인


![image](https://user-images.githubusercontent.com/35188271/108290434-bc880100-71d3-11eb-8929-1a45c433239d.png)
　  
　  
      
      
    
　  
　  
    

# Circuit Breaker
```
1. 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함.  
2. 시나리오는 예약(reservation)-->예치금 결제(deposit) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 예치금 결제 요청이 과도할 경우 CB 를 통하여 장애격리.  
3. Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 300 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```

    
　  
- 서킷브레이크 적용 전 (100% 적용)

![image](https://user-images.githubusercontent.com/35188271/108315303-8f047d00-71fe-11eb-83b8-7807ccefc645.png)
 　  


- application.yml 설정

![image](https://user-images.githubusercontent.com/35188271/108290590-112b7c00-71d4-11eb-92d6-840c5d3f551c.png)


 

　  

- 피호출 서비스(결제서비스:payment) 의 임의 부하 처리  Reservation.java(entity)

![image](https://user-images.githubusercontent.com/35188271/108290662-38824900-71d4-11eb-8ed0-7aa5d9134874.png)


    
　  
　  

`$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://52.231.94.89:8080/reservations POST {"restaurantNo": "10", "day":"20210214"}'`

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인 (동시사용자 100명, 60초 진행)

![image](https://user-images.githubusercontent.com/35188271/108318665-8bbfc000-7203-11eb-926c-a99ff003b8f3.png)


    
　  
　  

![image](https://user-images.githubusercontent.com/35188271/108318584-69c63d80-7203-11eb-8f25-dee673b35ab7.png)

`운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌`
    
　  
    
　  
　  
   
# Auto Scale(HPA)
```
1. 앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 
이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.  
2. 예치금 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 
넘어서면 replica 를 10개까지 늘려준다.
```

- 테스트를 위한 리소스 할당(reservation > deployment.yml)

![20210215_170036_22](https://user-images.githubusercontent.com/77368612/107920178-dcd77600-6faf-11eb-829a-afd2be2be901.png)
    
　  
　  

### autoscale out 설정 

- kubectl autoscale deploy reservation --min=1 --max=10 --cpu-percent=15

![image](https://user-images.githubusercontent.com/35188271/108320705-8152f580-7206-11eb-8d15-ed4fba68baba.png)

　  
- CB 에서 했던 방식대로 워크로드를 1분 동안 걸어준다.

`siege -c100 -t60S -r10 -v --content-type "application/json" 'http://52.231.32.13:8080/reservations POST {"cinemaNo": "AVATOR", "time":"2021-02-18_10:00~13:00"}'`

    

　  
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![image](https://user-images.githubusercontent.com/35188271/108323433-0390e900-720a-11eb-9742-8f0c3e6b8c35.png)

- deployment.yml 아래 설정 완료.　  
```
          resources:
            limits:
              cpu: 500m
            requests:
              cpu: 200m
```
　  
    

    
　  
    
　  
　  
   　  
　  
# Self-healing (Liveness Probe)

- deployment.yml 에 Liveness Probe 옵션 추가

![image](https://user-images.githubusercontent.com/77368612/107970557-99532b00-6ff4-11eb-82bf-312a9f8f3c8b.png)
    
　    
　  
　  
- reservation 서비스의 liveness가 발동되어 3번 retry 시도 한 부분 확인
- 
![image](https://user-images.githubusercontent.com/35188271/108305154-bf432000-71ec-11eb-8681-63b32bd68317.png)


