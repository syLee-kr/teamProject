import pandas as pd
import datetime
import json
import random
import os
import sys
import io

# 표준 출력과 입력 인코딩을 UTF-8로 설정
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')

def recommend_meals(data, categories, adjusted_bmr, selected_date_str):
    """날짜와 시간을 고려하여 식단을 추천하는 함수"""
    try:
        selected_date = datetime.datetime.strptime(selected_date_str, "%Y-%m-%d").date()
        print(f"Parsed selected_date: {selected_date}", file=sys.stderr)
    except ValueError:
        return {"status": "error", "message": "잘못된 날짜 형식입니다."}

    now = datetime.datetime.now()
    today = now.date()
    current_hour = now.hour
    print(f"Current datetime: {now}, today: {today}, current_hour: {current_hour}", file=sys.stderr)

    recommended_meals = {}

    if selected_date <= today:
        if selected_date < today or (selected_date == today and current_hour < 8):
            print("Entering block: selected_date < today or (selected_date == today and current_hour < 8)", file=sys.stderr)
            recommended_meals["breakfast"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        elif selected_date == today:
            if current_hour < 12:
                print("Entering block: selected_date == today and current_hour < 12", file=sys.stderr)
                recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            elif current_hour < 16:
                print("Entering block: selected_date == today and current_hour < 16", file=sys.stderr)
                recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            else:
                print("Entering block: selected_date == today and current_hour >= 16", file=sys.stderr)
                return {"status": "info", "message": "식단 추천 시간이 아닙니다."}  # 식단 추천 X
    else:
        print("Entering block: selected_date > today (future date)", file=sys.stderr)
        # 미래 날짜에 대한 추천 식단 처리 로직 추가
        recommended_meals["breakfast"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)

    return {"status": "success", "data": recommended_meals}

def recommend_meal_by_category(data, categories, target_calories):
    """카테고리 및 칼로리를 고려하여 식단을 추천하는 함수"""
    selected_foods = []
    available_categories = []

    # 카테고리 리스트 출력
    print(f"Available categories in data: {data['식품대분류명'].unique()}", file=sys.stderr)

    # 카테고리 값 순회
    for category in categories.values():
        filtered_data = data[data["식품대분류명"] == category]
        if not filtered_data.empty:
            available_categories.append(category)
        else:
            print(f"No foods found for category: {category}", file=sys.stderr)

    if not available_categories:
        return {"status": "error", "message": f"선택된 카테고리({categories})에 해당하는 식품이 없습니다."}

    for category in available_categories:
        filtered_data = data[data["식품대분류명"] == category]
        # 필수 필드에 NaN이 있는 식품 제외
        filtered_data = filtered_data.dropna(subset=["총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)", "식품중량"])
        if filtered_data.empty:
            print(f"No complete data found for category: {category}", file=sys.stderr)
            continue
        selected_food = filtered_data.sample(n=1)  # 랜덤하게 하나 선택
        validate_food_data(selected_food)  # 데이터 검증
        selected_foods.append(selected_food)

    if not selected_foods:
        return {"status": "error", "message": "선택된 카테고리에 대한 완전한 식품 데이터가 없습니다."}

    # 칼로리 조정 로직 (오차 범위 내에 맞추기)
    total_calories = sum(
        food["총 에너지(kcal)"].iloc[0] if not pd.isna(food["총 에너지(kcal)"].iloc[0]) else 0
        for food in selected_foods
    )
    tolerance = target_calories * 0.02  # 오차 범위 (2%)
    print(f"Initial total_calories: {total_calories}, target_calories: {target_calories}, tolerance: {tolerance}", file=sys.stderr)

    while not (target_calories - tolerance <= total_calories <= target_calories + tolerance):
        if total_calories > target_calories + tolerance:
            remove_index = random.randint(0, len(selected_foods) - 1)
            print(f"Removing food at index: {remove_index}", file=sys.stderr)
            removed_cal = selected_foods[remove_index]["총 에너지(kcal)"].iloc[0]
            total_calories -= removed_cal if not pd.isna(removed_cal) else 0
            del selected_foods[remove_index]
            if not selected_foods:
                return {"status": "error", "message": "적절한 칼로리를 맞출 수 없습니다."}
        elif total_calories < target_calories - tolerance and len(available_categories) > len(selected_foods):
            diff_categories = [i for i in available_categories if i not in [food["식품대분류명"].iloc[0] for food in selected_foods]]
            if not diff_categories:
                break
            new_category = random.choice(diff_categories)
            print(f"Adding food from new category: {new_category}", file=sys.stderr)
            filtered_data = data[data["식품대분류명"] == new_category]
            # 필수 필드에 NaN이 있는 식품 제외
            filtered_data = filtered_data.dropna(subset=["총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)", "식품중량"])
            if filtered_data.empty:
                print(f"No complete data found for category: {new_category}", file=sys.stderr)
                continue
            new_food = filtered_data.sample(n=1)
            added_cal = new_food["총 에너지(kcal)"].iloc[0] if not pd.isna(new_food["총 에너지(kcal)"].iloc[0]) else 0
            total_calories += added_cal
            selected_foods.append(new_food)
            print(f"Updated total_calories: {total_calories}", file=sys.stderr)
        else:
            break

    # 선택된 음식 정보 추출
    meal_info = []
    if selected_foods:
        for food in selected_foods:
            meal_info.append({
                "식품명": food["식품명"].iloc[0],
                "식품중량": float(food["식품중량"].iloc[0]) if not pd.isna(food["식품중량"].iloc[0]) else None,
                "총 에너지(kcal)": float(food["총 에너지(kcal)"].iloc[0]) if not pd.isna(food["총 에너지(kcal)"].iloc[0]) else None,
                "총 단백질(g)": float(food["총 단백질(g)"].iloc[0]) if not pd.isna(food["총 단백질(g)"].iloc[0]) else None,
                "총 지방(g)": float(food["총 지방(g)"].iloc[0]) if not pd.isna(food["총 지방(g)"].iloc[0]) else None,
                "총 탄수화물(g)": float(food["총 탄수화물(g)"].iloc[0]) if not pd.isna(food["총 탄수화물(g)"].iloc[0]) else None
            })
        return meal_info
    else:
        return {"status": "error", "message": "식단 추천에 실패했습니다."}

def validate_food_data(food):
    """식품 데이터의 유효성을 검증하는 함수"""
    required_fields = ["식품명", "식품중분류명", "식품대분류명", "식품중량", "총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)"]
    for field in required_fields:
        if field not in food.columns or pd.isna(food[field].iloc[0]):
            print(f"Warning: 식품 '{food['식품명'].iloc[0]}'의 '{field}' 필드가 비어 있습니다.", file=sys.stderr)
    # 추가적인 검증 로직을 여기에 추가할 수 있습니다.

def main():
    # JSON 입력 데이터 처리 (stdin에서 읽기)
    try:
        raw_input = sys.stdin.read()
        print(f"Received raw input: {raw_input}", file=sys.stderr)

        input_data = json.loads(raw_input)
        print(f"Parsed input JSON: {input_data}", file=sys.stderr)

        # 필요한 데이터 추출
        categories = input_data.get('categories', {})
        adjusted_bmr = input_data.get('bmr', None)
        selected_date_str = input_data.get('selectedDate', None)

        if not categories or adjusted_bmr is None or not selected_date_str:
            raise ValueError("categories, bmr, or selectedDate is missing from input JSON")

    except (json.JSONDecodeError, ValueError) as e:
        error_message = {"status": "error", "message": f"JSON 처리 오류: {str(e)}"}
        print(json.dumps(error_message, ensure_ascii=False))
        sys.exit(1)

    # 데이터 파일 로드
    script_dir = os.path.dirname(__file__)
    file_path = os.path.join(script_dir, 'data', 'foodDB.xlsx')  # 파일 이름 변경
    print(f"Resolved file path: {file_path}", file=sys.stderr)
    try:
        data = pd.read_excel(file_path, sheet_name=0, engine='openpyxl')
        print("Successfully loaded foodDB.xlsx", file=sys.stderr)
    except FileNotFoundError:
        print(json.dumps({"status": "error", "message": f"파일을 찾을 수 없습니다: {file_path}"}, ensure_ascii=False))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({"status": "error", "message": f"파일 로드 중 오류 발생: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)

    # 추천 결과 생성
    recommend_result = recommend_meals(data, categories, adjusted_bmr, selected_date_str)
    print(json.dumps(recommend_result, ensure_ascii=False))  # stdout으로 JSON 출력

if __name__ == "__main__":
    main()
