import pandas as pd
import datetime
import json
import random

def recommend_meals(data, categories, adjusted_bmr, selected_date_str):
    """날짜와 시간을 고려하여 식단을 추천하는 함수"""
    try:
        selected_date = datetime.datetime.strptime(selected_date_str, "%Y-%m-%d").date()
    except ValueError:
        print("잘못된 날짜 형식입니다.")
        return None

    now = datetime.datetime.now()
    today = now.date()
    current_hour = now.hour

    recommended_meals = {}

    if selected_date < today or (selected_date == today and current_hour < 8):
        # 하루 이상 차이나거나, 오늘 날짜이면서 오전 8시 이전인 경우
        recommended_meals["breakfast"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)

    elif selected_date == today:
        if current_hour < 12:
            recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        elif current_hour < 16:
            recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        else:
            return None # 식단 추천 x
    else:
        return None # 선택된 날짜가 미래일 경우 추천 X

    return recommended_meals

def recommend_meal_by_category(data, categories, target_calories):
    """카테고리 및 칼로리를 고려하여 식단을 추천하는 함수"""
    selected_foods = []
    available_categories = []

    for category in categories:
        filtered_data = data[data["식품대분류명"] == category]
        if not filtered_data.empty:
            available_categories.append(category)

    if not available_categories:
        return None

    for category in available_categories:
        filtered_data = data[data["식품대분류명"] == category]
        selected_food = filtered_data.sample(n=1)  # 랜덤하게 하나 선택
        selected_foods.append(selected_food)

    # 칼로리 조정 로직 (오차 범위 내에 맞추기)
    total_calories = sum(food["총 에너지(kcal)"].iloc[0] for food in selected_foods)
    tolerance = target_calories * 0.02  # 오차 범위 (2%)

    while not (target_calories - tolerance <= total_calories <= target_calories + tolerance):
        if total_calories > target_calories + tolerance :
            remove_index = random.randint(0, len(selected_foods) - 1)
            total_calories -= selected_foods[remove_index]["총 에너지(kcal)"].iloc[0]
            del selected_foods[remove_index]
            if not selected_foods:
                return None
        elif total_calories < target_calories - tolerance and len(available_categories) > len(selected_foods):
            diff_categories = [i for i in available_categories if i not in [food["식품대분류명"].iloc[0] for food in selected_foods]]
            new_category = random.choice(diff_categories)
            filtered_data = data[data["식품대분류명"] == new_category]
            new_food = filtered_data.sample(n=1)
            total_calories += new_food["총 에너지(kcal)"].iloc[0]
            selected_foods.append(new_food)
        else:
            break

    # 선택된 음식 정보 추출
    meal_info = []
    if selected_foods:
        for food in selected_foods:
            meal_info.append({
                "식품명": food["식품명"].iloc[0],
                "식품중량": food["식품중량"].iloc[0],
                "총 에너지(kcal)": food["총 에너지(kcal)"].iloc[0],
                "총 단백질(g)": food["총 단백질(g)"].iloc[0],
                "총 지방(g)": food["총 지방(g)"].iloc[0],
                "총 탄수화물(g)": food["총 탄수화물(g)"].iloc[0]
            })
        return meal_info
    else:
        return None

def main():
    file_path = './data/음식DB.xlsx'
    try:
        data = pd.read_excel(file_path, sheet_name=0, engine='openpyxl')
    except FileNotFoundError:
        print(f"경로에서 파일을 찾을 수 없습니다: {file_path}")
        return
    except Exception as e:
        print(f"엑셀 파일을 읽는 중 오류가 발생했습니다: {e}")
        return

    import sys
    if len(sys.argv) > 1:
        try:
            input_data = json.loads(sys.argv[1])
            selected_date_str = input_data.get('selectedDate')
            categories = input_data.get('categories')
            adjusted_bmr = input_data.get('bmr')
            if selected_date_str is None or categories is None or adjusted_bmr is None:
                print("selectedDate, categories, bmr 값이 모두 필요합니다.")
                return
        except json.JSONDecodeError:
            print("잘못된 JSON 형식입니다.")
            return
    else:
        print("인자가 필요합니다.")
        return

    recommend_result = recommend_meals(data, categories, adjusted_bmr, selected_date_str)
    print(json.dumps(recommend_result, ensure_ascii=False))

if __name__ == "__main__":
    main()