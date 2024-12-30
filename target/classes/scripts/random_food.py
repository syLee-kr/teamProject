import pandas as pd
import datetime
import json
import random
import os
import sys
import io
from pytz import timezone

# 표준 출력과 입력 인코딩을 UTF-8로 설정
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')

def recommend_meals(data, categories, adjusted_bmr, selected_date_str):
    try:
        selected_date = datetime.datetime.strptime(selected_date_str, "%Y-%m-%d").date()
        print(f"Parsed selected_date: {selected_date}", file=sys.stderr)
    except ValueError:
        return {"status": "error", "message": "잘못된 날짜 형식입니다."}

    # 현재 시간을 한국 시간대로 변환
    kst = timezone('Asia/Seoul')
    now = datetime.datetime.now(tz=kst)
    today = now.date()
    current_hour = now.hour
    print(f"Debug: selected_date = {selected_date}, today = {today}, current_hour = {current_hour} (KST)", file=sys.stderr)

    recommended_meals = {}

    try:
        if selected_date > today:
            print("Future date detected: Recommending all meals", file=sys.stderr)
            recommended_meals["breakfast"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
        elif selected_date <= today:
            if selected_date < today or (selected_date == today and current_hour < 8):
                recommended_meals["breakfast"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
            elif selected_date == today:
                if current_hour < 14:
                    recommended_meals["lunch"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                    recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                elif current_hour < 24:
                    recommended_meals["dinner"] = recommend_meal_by_category(data, categories, adjusted_bmr / 3)
                else:
                    print("No meals to recommend at this time.", file=sys.stderr)
                    return {"status": "info", "message": "식단 추천 시간이 아닙니다."}
        else:
            print("Unexpected case in date handling.", file=sys.stderr)
            return {"status": "error", "message": "날짜 처리 중 오류가 발생했습니다."}
    except Exception as e:
        print(f"Error during meal recommendation: {str(e)}", file=sys.stderr)
        return {"status": "error", "message": "식단 추천 중 오류가 발생했습니다."}

    # 추천 결과 확인
    if not recommended_meals:
        print("No meals were recommended.", file=sys.stderr)
        return {"status": "error", "message": "추천된 식단이 없습니다."}

    return {"status": "success", "data": recommended_meals}

def recommend_meal_by_category(data, categories, target_calories):
    selected_foods = []
    available_categories = []

    print(f"Available categories in data: {data['식품대분류명'].unique()}", file=sys.stderr)

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
        filtered_data = filtered_data.dropna(subset=["총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)", "식품중량"])
        if filtered_data.empty:
            print(f"No complete data found for category: {category}", file=sys.stderr)
            continue
        selected_food = filtered_data.sample(n=1)
        validate_food_data(selected_food)
        selected_foods.append(selected_food)

    if not selected_foods:
        return {"status": "error", "message": "선택된 카테고리에 대한 완전한 식품 데이터가 없습니다."}

    total_calories = sum(
        food["총 에너지(kcal)"].iloc[0] if not pd.isna(food["총 에너지(kcal)"].iloc[0]) else 0
        for food in selected_foods
    )
    tolerance = target_calories * 0.02
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
            filtered_data = data[data["식품대분류명"] == new_category]
            filtered_data = filtered_data.dropna(subset=["총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)", "식품중량"])
            if filtered_data.empty:
                continue
            new_food = filtered_data.sample(n=1)
            added_cal = new_food["총 에너지(kcal)"].iloc[0] if not pd.isna(new_food["총 에너지(kcal)"].iloc[0]) else 0
            total_calories += added_cal
            selected_foods.append(new_food)
        else:
            break

    meal_info = []
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

def validate_food_data(food):
    required_fields = ["식품명", "식품중분류명", "식품대분류명", "식품중량", "총 에너지(kcal)", "총 단백질(g)", "총 지방(g)", "총 탄수화물(g)"]
    for field in required_fields:
        if field not in food.columns or pd.isna(food[field].iloc[0]):
            print(f"Warning: 식품 '{food['식품명'].iloc[0]}'의 '{field}' 필드가 비어 있습니다.", file=sys.stderr)

def save_meal_plan(selected_date, meal_plan):
    """선택된 날짜와 함께 식단을 저장하는 함수"""
    save_data = {
        "date": selected_date.isoformat(),
        "meals": meal_plan
    }
    save_file = 'meal_plans.json'

    # 기존 데이터 불러오기
    if os.path.exists(save_file):
        try:
            with open(save_file, 'r', encoding='utf-8') as f:
                existing_data = json.load(f)
        except json.JSONDecodeError:
            print(f"Warning: 기존 저장 파일이 비어있거나 유효하지 않습니다. 새로 작성합니다.", file=sys.stderr)
            existing_data = []
    else:
        existing_data = []

    # 동일 날짜의 식단이 이미 존재하는지 확인
    for entry in existing_data:
        if entry["date"] == selected_date.isoformat():
            print(f"Info: {selected_date.isoformat()}의 식단이 이미 저장되어 있습니다. 덮어씌웁니다.", file=sys.stderr)
            existing_data.remove(entry)
            break

    # 새로운 식단 추가
    existing_data.append(save_data)

    # 데이터 저장
    try:
        with open(save_file, 'w', encoding='utf-8') as f:
            json.dump(existing_data, f, ensure_ascii=False, indent=4)
        print(f"Meal plan saved for {selected_date.isoformat()}", file=sys.stderr)
    except Exception as e:
        print(json.dumps({"status": "error", "message": f"식단 저장 중 오류 발생: {str(e)}"}, ensure_ascii=False))
        sys.exit(1)

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

    # 추천 결과가 성공적이라면 저장
    if recommend_result["status"] == "success":
        try:
            selected_date = datetime.datetime.strptime(selected_date_str, "%Y-%m-%d").date()
            save_meal_plan(selected_date, recommend_result["data"])
        except Exception as e:
            print(json.dumps({"status": "error", "message": f"식단 저장 중 오류 발생: {str(e)}"}, ensure_ascii=False))
            sys.exit(1)

if __name__ == "__main__":
    main()
