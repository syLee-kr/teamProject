import sys
import json
import pandas as pd
import random
from datetime import datetime

# 엑셀 파일 경로
file_path = './data/20240807_음식DB.xlsx'

# 엑셀 데이터 로드
data = pd.read_excel(file_path, sheet_name=0, engine='openpyxl')

def get_meal_time(selected_date_str):
    now = datetime.now()
    current_date_str = now.strftime("%Y-%m-%d")

    selected_date = datetime.strptime(selected_date_str, "%Y-%m-%d")
    current_date = datetime.strptime(current_date_str, "%Y-%m-%d")

    day_diff = (selected_date - current_date).days

    if day_diff >= 1:
        return ["breakfast", "lunch", "dinner"]
    elif day_diff == 0:
        current_time = now.hour + now.minute/60.0
        if current_time < 9:
            return ["breakfast", "lunch", "dinner"]
        elif current_time < 14:
            return ["lunch", "dinner"]
        elif current_time < 16:
            return ["dinner"]
        else:
            return []
    else:
        return []

def get_random_meal(categories, meal_type, bmr):
    if meal_type == "breakfast":
        min_ratio, max_ratio = 0.25, 0.30
    else:
        min_ratio, max_ratio = 0.30, 0.40

    min_kcal = bmr * min_ratio
    max_kcal = bmr * max_ratio

    category_items = {}
    for category_key, values in categories.items():
        if values:
            filtered = data[data["식품대분류명"].isin(values)]
            if filtered.empty:
                category_items[category_key] = []
            else:
                category_items[category_key] = filtered.to_dict('records')
        else:
            category_items[category_key] = []

    attempts = 200
    for _ in range(attempts):
        chosen = {}
        total_kcal = 0
        valid = True
        for cat, items in category_items.items():
            if not items:
                valid = False
                break
            item = random.choice(items)
            if '총 에너지(kcal)' not in item:
                valid = False
                break
            chosen[cat] = item
            total_kcal += item['총 에너지(kcal)']

        if valid and min_kcal <= total_kcal <= max_kcal:
            return chosen

    return None

def calculate_total_kcal(meals):
    total_kcal = 0
    for meal, meal_items in meals.items():
        for cat, item in meal_items.items():
            total_kcal += item['총 에너지(kcal)']
    return total_kcal

def meets_bmi_constraints(total_kcal, bmr, bmi):
    if bmi >= 23:
        return (0.9 * bmr) <= total_kcal <= (1.0 * bmr)
    else:
        return (1.0 * bmr) <= total_kcal <= (1.1 * bmr)

if __name__ == '__main__':
    input_data = json.loads(sys.argv[1])
    bmr = input_data.get("bmr", None)
    bmi = input_data.get("bmi", None)
    categories = input_data.get("categories", {})
    selected_date = input_data.get("selectedDate", None)

    meal_types = get_meal_time(selected_date)

    if not meal_types:
        output = {
            "selectedDate": selected_date,
            "bmr": bmr,
            "bmi": bmi,
            "foodResult": None
        }
        print(json.dumps(output, ensure_ascii=False))
        sys.exit(0)

    attempts = 300
    final_meals = None
    for _ in range(attempts):
        temp_meals = {}
        valid = True
        for mt in meal_types:
            meal_choice = get_random_meal(categories, mt, bmr)
            if meal_choice is None:
                valid = False
                break
            temp_meals[mt] = meal_choice

        if not valid:
            continue

        total_kcal = calculate_total_kcal(temp_meals)
        if meets_bmi_constraints(total_kcal, bmr, bmi):
            final_meals = temp_meals
            break

    # final_meals를 원하는 형태로 가공
    # 여기서는 예시로 mealType, mainFood, sideDishes, dessert 등의 필드로 가정
    # 실제 로직에 맞게 변경 필요
    if final_meals:
        # 예시로 breakfast 결과만 추출. 실제로는 breakfast, lunch, dinner 모두 처리 필요
        # 여기서는 단순 예시로 한 끼 식사만 있다고 가정하고 mainFood, sideDishes 등을 임의로 할당.
        # 실제로는 final_meals 딕셔너리를 활용해 실제 데이터 생성해야 함.

        # 예시: 첫 mealType의 카테고리 중 첫 번째 항목을 mainFood로 가정
        # sideDishes, dessert 등도 실제 로직에 따라 할당
        example_meal_type = list(final_meals.keys())[0]
        meal_data = final_meals[example_meal_type]
        # meal_data는 { "category1": {...}, "category2": {...}, ... } 형태
        # mainFood 예: category1에서 선택된 식품명
        mainFood = meal_data.get("category1", {}).get("식품명", "없음")
        sideDishes = [meal_data.get("category2", {}).get("식품명", "없음"), meal_data.get("category3", {}).get("식품명", "없음")]
        dessert = meal_data.get("category4", {}).get("식품명", "없음")

        total_kcal = calculate_total_kcal(final_meals)

        output = {
            "selectedDate": selected_date,
            "bmr": bmr,
            "bmi": bmi,
            "foodResult": {
                "mealType": example_meal_type,
                "mainFood": mainFood,
                "sideDishes": sideDishes,
                "dessert": dessert,
                "category": "예시카테고리",
                "calories": total_kcal,
                "protein": 20,  # 예시 값
                "carbohydrates": 50, # 예시 값
                "fat": 10 # 예시 값
            }
        }
    else:
        output = {
            "selectedDate": selected_date,
            "bmr": bmr,
            "bmi": bmi,
            "foodResult": None
        }

    print(json.dumps(output, ensure_ascii=False))
