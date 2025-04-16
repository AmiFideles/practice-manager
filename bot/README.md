# Task tracker:
## APIs

### 1. Approves
* Загрузка файла со студентами (whitelist, admin) ✅
  * /api/students/upload
* Отправить запрос на аппрув (студент) ✅
  * user-controller register POST - студент регается
* Подтвердить аппрув (админ) ✅
* Получить список студентов со статусами регистрации ✅

* Approval-status-controller
  * Put /api/approvals/{isuNumber} - ✅
  * Get /api/approvals ✅
  * POST /api/approvals - ✅(Вроде)
  * GET /api/approvals/student-status - ✅
  * GET /api/approvals/status - ✅
  * GET /api/approvals/excel - ✅
