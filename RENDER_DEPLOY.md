# Deploy KYN AURA len Render bang Docker

## 1. Chuan bi PostgreSQL tren Render

App da duoc doi sang PostgreSQL de dung database managed cua Render.

Neu dung Blueprint (`render.yaml`), Render se tao database `kyn-aura-db` va tu gan bien moi truong cho web service.

Neu setup thu cong, tao PostgreSQL database truoc:

```text
kin_shop
```

## 2. Cau hinh bien moi truong tren Render

Khi tao Web Service thu cong tren Render, them cac bien moi truong theo thong tin cua PostgreSQL database:

```text
DB_HOST=<postgres-host>
DB_PORT=5432
DB_NAME=<database-name>
DB_USERNAME=<postgres-user>
DB_PASSWORD=<postgres-password>
JAVA_OPTS=-Xms128m -Xmx384m
```

Neu muon dung full JDBC URL thay vi cac bien tren, co the set:

```text
DB_URL=jdbc:postgresql://<postgres-host>:5432/<database-name>
```

## 3. Deploy bang render.yaml

Repo da co `render.yaml`, nen co the deploy bang Blueprint tren Render:

1. Push source code len GitHub.
2. Vao Render Dashboard.
3. Chon New > Blueprint.
4. Chon GitHub repository cua project.
5. Render se tao PostgreSQL database va gan env vars tu `render.yaml`.
6. Deploy.

## 4. Deploy thu cong

Neu khong dung Blueprint:

1. Vao Render Dashboard.
2. Chon New > Web Service.
3. Chon repository.
4. Runtime: Docker.
5. Dockerfile path: `./Dockerfile`.
6. Health check path: `/login`.
7. Them bien moi truong PostgreSQL nhu muc 2.
8. Deploy.

## 5. Tai khoan dang nhap mac dinh

Sau khi app chay lan dau, he thong tu tao admin:

```text
Username: admin
Password: admin123
```

Nen doi mat khau hoac tao user moi va phan quyen lai sau khi deploy.
