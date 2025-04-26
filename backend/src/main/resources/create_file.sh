#!/bin/bash

# Проверка наличия аргумента
if [ $# -eq 0 ]; then
    echo "Ошибка: Не указана целевая директория"
    echo "Использование: $0 <путь/к/директории>"
    exit 1
fi

target_dir=$1

# Проверка существования директории
if [ ! -d "$target_dir" ]; then
    echo "Ошибка: Директория '$target_dir' не существует"
    exit 1
fi

# Нормализация пути (преобразование относительного пути в абсолютный)
normalized_dir=$(realpath "$target_dir")

# Список файлов для создания
files=(
    "V1.1__rollback_user.sql"
    "V1.2__rollback_direction.sql"
    "V1.3__rollback_organization.sql"
    "V1.4__rollback_study_group.sql"
    "V1.5__rollback_supervisor.sql"
    "V1.6__rollback_student.sql"
    "V1.7__rollback_activity_code.sql"
    "V1.8__rollback_apply.sql"
)

# Счётчик созданных файлов
created=0

echo "Создание файлов миграции в директории: $normalized_dir"

# Создание файлов
for file in "${files[@]}"; do
    full_path="$normalized_dir/$file"

    if [[ -f "$full_path" ]]; then
        echo "  Файл $file уже существует, пропускаем"
    else
        touch "$full_path"
        echo "  Создан файл: $full_path"
        ((created++))
    fi
done

# Итоговый отчёт
echo -e "\nГотово! Создано новых файлов: $created из ${#files[@]}"
echo "Все файлы созданы в директории: $normalized_dir"