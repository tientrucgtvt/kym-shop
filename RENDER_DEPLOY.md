# Deploy KYN AURA len Render bang Docker

## 1. Chuan bi database MySQL

Render khong co MySQL managed database mac dinh, nen can dung MySQL ben ngoai nhu Aiven, Railway, PlanetScale, Clever Cloud, hoac server MySQL rieng.

Tao database, vi du:

```text
kin_shop
```

## 2. Cau hinh bien moi truong tren Render

Khi tao Web Service tren Render, chon Docker va them cac bien moi truong:

```text
DB_URL=jdbc:mysql://<host>:<port>/<database>?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=<mysql-user>
DB_PASSWORD=<mysql-password>
JAVA_OPTS=-Xms128m -Xmx384m
```

Neu MySQL provider yeu cau SSL rieng, dieu chinh tham so `useSSL` theo tai lieu cua provider do.

## 3. Deploy bang render.yaml

Repo da co `render.yaml`, nen co the deploy bang Blueprint tren Render:

1. Push source code len GitHub.
2. Vao Render Dashboard.
3. Chon New > Blueprint.
4. Chon GitHub repository cua project.
5. Dien `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` khi Render yeu cau.
6. Deploy.

## 4. Deploy thu cong

Neu khong dung Blueprint:

1. Vao Render Dashboard.
2. Chon New > Web Service.
3. Chon repository.
4. Runtime: Docker.
5. Dockerfile path: `./Dockerfile`.
6. Health check path: `/login`.
7. Them bien moi truong nhu muc 2.
8. Deploy.

## 5. Tai khoan dang nhap mac dinh

Sau khi app chay lan dau, he thong tu tao admin:

```text
Username: admin
Password: admin123
```

Nen doi mat khau hoac tao user moi va phan quyen lai sau khi deploy.
