package com.devloopsx.chronelis.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
	// General errors
	UNCATEGORIZED_EXCEPTION(9999, HttpStatus.INTERNAL_SERVER_ERROR,
			"Ngoại lệ chưa được phân loại do lỗi hệ thống chưa được xử lý"),
	INVALID_KEY(1001, HttpStatus.BAD_REQUEST,
			"Tên trường không hợp lệ"),
	UNAUTHENTICATED(1002, HttpStatus.UNAUTHORIZED,
			"Thông tin đăng nhập không hợp lệ"),
	UNAUTHORIZED(1003, HttpStatus.FORBIDDEN,
			"Truy cập bị từ chối, bạn không có quyền truy cập tính năng này"),
	SEND_EMAIL_ERROR(
			1004, HttpStatus.INTERNAL_SERVER_ERROR,
			"Gửi email thất bại"),
	TOKEN_EXPIRED(1005, HttpStatus.UNAUTHORIZED,
			"Token truy cập đã hết hạn"),
	INVALID_TOKEN(1006,
			HttpStatus.UNAUTHORIZED,
			"Token truy cập không hợp lệ"),
	TOKEN_REVOKED(1007,
			HttpStatus.UNAUTHORIZED,
			"Token đã bị thu hồi do người dùng đã đăng xuất"),
	NOT_FOUND_ROUTE(
			1008, HttpStatus.NOT_FOUND,
			"Không tìm thấy tài nguyên"),
	DATE_FORMAT_INSTANT(
			1009, HttpStatus.BAD_REQUEST,
			"Định dạng ngày phải theo chuẩn ISO-8601"),
	MISSING_TOKEN(
			1010,
			HttpStatus.UNAUTHORIZED,
			"Thiếu token truy cập"),
	SYSTEM_EMAIL_CANNOT_BE_DELETED(
			1011,
			HttpStatus.BAD_REQUEST,
			"Không thể xoá hoặc chỉnh sửa email mặc định của hệ thống"),
	SYSTEM_ROLE_CANNOT_BE_DELETED(
			1012,
			HttpStatus.BAD_REQUEST,
			"Không thể xoá hoặc chỉnh sửa vai trò mặc định của hệ thống"),
	INVALID_PHONE_NUMBER(
			1012,
			HttpStatus.BAD_REQUEST,
			"Số điện thoại không hợp lệ"),
	PHONE_NUMBER_FORMAT_ERROR(
			1013,
			HttpStatus.BAD_REQUEST,
			"Định dạng số điện thoại không đúng"),
	PHONE_NUMBER_NOT_SUPPORTED(
			1014,
			HttpStatus.BAD_REQUEST,
			"Số điện thoại không được hỗ trợ"),
	INVALID_HTTP_METHOD(
			1015,
			HttpStatus.BAD_REQUEST,
			"Phương thức HTTP không hợp lệ. Chỉ hỗ trợ các phương thức sau: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS"),
	INVALID_REQUEST_DATA(
			1016,
			HttpStatus.BAD_REQUEST,
			"Dữ liệu yêu cầu không hợp lệ. Vui lòng kiểm tra định dạng và giá trị các trường rồi thử lại"),
	RESOURCE_NOT_FOUND(
			1017,
			HttpStatus.NOT_FOUND,
			"Không tìm thấy tài nguyên"),
	INVALID_OWNER_ID(
			1018,
			HttpStatus.BAD_REQUEST,
			"ID chủ sở hữu không hợp lệ"),
	UNAUTHORIZED_ACCESS(
			1019,
			HttpStatus.UNAUTHORIZED,
			"Người dùng không có quyền truy cập tài nguyên này"),
	INVALID_SORT_FIELD(
			1020,
			HttpStatus.BAD_REQUEST,
			"Trường sắp xếp không hợp lệ"),
	NO_UPDATE_PROVIDED(
			1021,
			HttpStatus.BAD_REQUEST,
			"Không có thay đổi được cung cấp để cập nhật"),
	TOO_MANY_REQUESTS(
			1022,
			HttpStatus.TOO_MANY_REQUESTS,
			"Gửi quá nhiều yêu cầu"),
	VNPAY_CALLBACK_VERIFICATION_FAILED(
			1099,
			HttpStatus.BAD_REQUEST,
			"Xác minh callback VNPay thất bại"),
	VNPAY_TXN_REF_MISMATCH(
			1100,
			HttpStatus.BAD_REQUEST,
			"Mã giao dịch VNPay không khớp"),

	// Module auth errors
	EMAIL_NOT_BLANK(1101, HttpStatus.BAD_REQUEST, "Email không được để trống"), EMAIL_INVALID(1102,
			HttpStatus.BAD_REQUEST, "Định dạng email không hợp lệ"),
	EMAIL_PROVIDER_INVALID(1103,
			HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ nhà cung cấp email Gmail và Yopmail"),
	PASSWORD_NOT_BLANK(1104,
			HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống"),
	PASSWORD_INVALID_FORMAT(1105,
			HttpStatus.BAD_REQUEST,
			"Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt"),
	FIRSTNAME_NOT_BLANK(
			1106, HttpStatus.BAD_REQUEST,
			"Tên không được để trống"),
	FIRSTNAME_INVALID_LENGTH(1107,
			HttpStatus.BAD_REQUEST,
			"Tên phải có độ dài từ 2 đến 50 ký tự"),
	LASTNAME_NOT_BLANK(1108,
			HttpStatus.BAD_REQUEST,
			"Họ không được để trống"),
	LASTNAME_INVALID_LENGTH(1109,
			HttpStatus.BAD_REQUEST,
			"Họ phải có độ dài từ 2 đến 50 ký tự"),
	PHONE_NUMBER_NOT_BLANK(
			1110, HttpStatus.BAD_REQUEST,
			"Số điện thoại không được để trống"),
	PHONE_NUMBER_VN_INVALID(
			1111, HttpStatus.BAD_REQUEST,
			"Số điện thoại Việt Nam không hợp lệ"),
	EMAIL_EXISTED(
			1112, HttpStatus.CONFLICT,
			"Đã tồn tại người dùng với email này trong hệ thống"),
	TOKEN_NOT_BLANK(
			1113,
			HttpStatus.BAD_REQUEST,
			"Token không được để trống"),
	PASSWORD_MISMATCH(
			1114,
			HttpStatus.UNAUTHORIZED,
			"Mật khẩu hiện tại không đúng"),
	NOT_VERIFIED_ACCOUNT_TWICE(
			1115,
			HttpStatus.BAD_REQUEST,
			"Bạn đã xác thực tài khoản trước đó"),
	NOT_VERIFIED_ACCOUNT(
			1116,
			HttpStatus.BAD_REQUEST,
			"Tài khoản của bạn chưa được xác thực. Vui lòng kiểm tra email hoặc yêu cầu gửi lại email xác thực"),
	PHONE_NUMBER_EXISTED(
			1117,
			HttpStatus.CONFLICT,
			"Đã tồn tại người dùng với số điện thoại này. Vui lòng sử dụng số khác"),
	NEW_PASSWORD_NOT_BLANK(
			1118,
			HttpStatus.BAD_REQUEST,
			"Mật khẩu mới không được để trống"),
	CONFIRM_PASSWORD_NOT_BLANK(
			1119,
			HttpStatus.BAD_REQUEST,
			"Xác nhận mật khẩu không được để trống"),
	PASSWORD_AND_CONFIRM_MISMATCH(
			1120,
			HttpStatus.BAD_REQUEST,
			"Mật khẩu và xác nhận mật khẩu không khớp"),
	EMAIL_OR_PHONE_REQUIRED(
			1121,
			HttpStatus.BAD_REQUEST,
			"Vui lòng nhập email hoặc số điện thoại để đăng nhập"),
	ONLY_EMAIL_OR_PHONE(
			1122,
			HttpStatus.BAD_REQUEST,
			"Chỉ được sử dụng một trong hai: email hoặc số điện thoại"),
	CONFIRM_PASSWORD_INVALID_FORMAT(
			1123,
			HttpStatus.BAD_REQUEST,
			"Xác nhận mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt"),

	// Module user errors
	USER_NOT_FOUND(1201, HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"), NICKNAME_INVALID_LENGTH(1202,
			HttpStatus.BAD_REQUEST, "Biệt danh phải có độ dài từ 2 đến 50 ký tự"),
	CURRENT_PASSWORD_NOT_BLANK(1203,
			HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không được để trống"),
	PASSWORD_SAME_AS_CURRENT(1204,
			HttpStatus.BAD_REQUEST,
			"Mật khẩu mới phải khác mật khẩu hiện tại"),
	NEW_PHONE_NUMBER_NOT_BLANK(1205,
			HttpStatus.BAD_REQUEST,
			"Số điện thoại mới không được để trống"),
	NEW_EMAIL_NOT_BLANK(1206,
			HttpStatus.BAD_REQUEST,
			"Email mới không được để trống"),
	NEW_EMAIL_SAME_BEFORE(1207,
			HttpStatus.BAD_REQUEST,
			"Email mới giống với email hiện tại, vui lòng sử dụng email khác"),
	NEW_PHONE_NUMBER_SAME_BEFORE(
			1208, HttpStatus.BAD_REQUEST,
			"Số điện thoại mới giống với số hiện tại, vui lòng sử dụng số khác"),
	USER_SAME_IS_VERIFY(
			1209, HttpStatus.BAD_REQUEST,
			"Người dùng đã được xác thực"),
	USER_ID_REQUIRED(
			1210, HttpStatus.BAD_REQUEST,
			"Yêu cầu ID người dùng"),
	USER_ALREADY_HAS_STAFF_ROLE(
			1211, HttpStatus.BAD_REQUEST,
			"Người dùng đã có vai trò USER rồi"),
	USER_CANNOT_BE_DELETED_HAS_ORDERS(
			1212,
			HttpStatus.BAD_REQUEST,
			"Không thể xóa người dùng này vì đã có đơn thuê trong hệ thống"),

	// Module role errors
	ROLE_NAME_NOT_FOUND(1301, HttpStatus.NOT_FOUND, "Không tìm thấy tên vai trò trong hệ thống"), ROLE_NAME_NOT_BLANK(
			1302, HttpStatus.BAD_REQUEST, "Tên vai trò không được để trống"),
	ROLE_NAME_EXISTED(1303,
			HttpStatus.CONFLICT,
			"Tên vai trò đã tồn tại trong hệ thống, vui lòng sử dụng tên khác"),
	ROLE_NOT_FOUND(1304,
			HttpStatus.NOT_FOUND, "Không tìm thấy vai trò"),
	DUPLICATE_ROLE_IDS(1305,
			HttpStatus.BAD_REQUEST,
			"Danh sách vai trò chứa ID trùng lặp, vui lòng kiểm tra lại"),
	PERMISSION_ALREADY_EXISTS_IN_ROLE(
			1306, HttpStatus.BAD_REQUEST,
			"Vai trò hiện tại đã có các quyền này"),
	ROLE_ALREADY_ASSIGNED(1307,
			HttpStatus.BAD_REQUEST,
			"Người dùng đã có vai trò này và không thể được gán lại"),
	ROLE_SAME_IS_ACTIVE(
			1308, HttpStatus.BAD_REQUEST,
			"Vai trò hiện tại đã được kích hoạt"),
	ROLE_IDS_NOT_BLANK(
			1309, HttpStatus.BAD_REQUEST,
			"Danh sách ID các vai trò không được để trống, vui lòng bổ sung thêm"),
	ROLE_NOT_IN_USER(
			1310, HttpStatus.BAD_REQUEST,
			"Người dùng hiện tại không có vai trò này"),

	// Module permission errors
	PERMISSION_NAME_NOT_BLANK(1401, HttpStatus.BAD_REQUEST,
			"Tên quyền không được để trống"),
	PERMISSION_API_PATH_NOT_BLANK(1402, HttpStatus.BAD_REQUEST,
			"Đường dẫn API không được để trống"),
	PERMISSION_MODULE_NOT_BLANK(1403, HttpStatus.BAD_REQUEST,
			"Tên module không được để trống"),
	PERMISSION_HTTP_METHOD_NOT_BLANK(1404,
			HttpStatus.BAD_REQUEST,
			"Phương thức HTTP không được để trống"),
	PERMISSION_NAME_EXISTED(1405,
			HttpStatus.CONFLICT,
			"Tên quyền đã tồn tại trong hệ thống, vui lòng sử dụng tên khác"),
	PERMISSION_PATH_AND_METHOD_EXISTED(
			1406, HttpStatus.CONFLICT,
			"Đường dẫn API và phương thức đã tồn tại trong hệ thống"),
	PERMISSION_NOT_FOUND(
			1407, HttpStatus.NOT_FOUND,
			"Không tìm thấy quyền"),
	DUPLICATE_PERMISSION_IDS(1408,
			HttpStatus.BAD_REQUEST,
			"Danh sách quyền chứa ID trùng lặp, vui lòng kiểm tra lại"),
	PERMISSION_IDS_NOT_BLANK(
			1409, HttpStatus.BAD_REQUEST,
			"Danh sách ID quyền không được để trống, vui lòng thêm một số ID"),
	PERMISSION_NOT_IN_ROLE(
			1410, HttpStatus.BAD_REQUEST,
			"Quyền không tồn tại trong vai trò hiện tại"),
	PERMISSION_MODULE_NOT_FOUND(
			1411, HttpStatus.NOT_FOUND,
			"Không tìm thấy module"),
	PERMISSION_ALREADY_IN_ANOTHER_MODULE(
			1412,
			HttpStatus.BAD_REQUEST,
			"Quyền đã thuộc về module khác"),
	PERMISSION_MODULE_NAME_EXISTED(
			1413,
			HttpStatus.CONFLICT,
			"Tên module đã tồn tại trong hệ thống, vui lòng sử dụng tên khác"),
	PERMISSION_MODULE_NAME_INVALID(
			1414,
			HttpStatus.BAD_REQUEST,
			"Tên module không hợp lệ, vui lòng sử dụng tên khác"),

	// Module upload files errors
	FILE_NOT_FOUND(1501, HttpStatus.NOT_FOUND, "Không tìm thấy tệp"), FILE_UPLOAD_FAILED(1502,
			HttpStatus.INTERNAL_SERVER_ERROR, "Tải tệp lên thất bại, vui lòng thử lại"),
	FILE_TYPE_NOT_ALLOWED(1503,
			HttpStatus.BAD_REQUEST, "Loại tệp không được cho phép"),
	FILE_TOO_LARGE(1504,
			HttpStatus.BAD_REQUEST, "Kích thước tệp vượt quá giới hạn cho phép"),
	FILE_TOO_SMALL(1505,
			HttpStatus.BAD_REQUEST,
			"Kích thước tệp quá nhỏ và không đáp ứng yêu cầu"),
	INVALID_FILE_NAME(1506,
			HttpStatus.BAD_REQUEST,
			"Tên tệp không hợp lệ, vui lòng kiểm tra lại"),
	FILE_DELETE_FAILED(1507,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Xoá tệp thất bại, vui lòng thử lại"),
	FILE_MOVE_FAILED(1508,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Di chuyển tệp thất bại, vui lòng thử lại"),
	MULTIPLE_FILE_UPLOAD_PARTIALLY_FAILED(
			1509, HttpStatus.PARTIAL_CONTENT,
			"Một hoặc một vài tệp tải lên thất bại, vui lòng kiểm tra lại"),
	FILE_ALREADY_EXISTS(
			1510, HttpStatus.CONFLICT,
			"Tệp đã tồn tại trong hệ thống, vui lòng đổi tên hoặc sử dụng tên khác"),
	FOLDER_NOT_FOUND(
			1511, HttpStatus.NOT_FOUND,
			"Không tìm thấy thư mục"),
	FOLDER_CREATION_FAILED(
			1512,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Tạo thư mục thất bại, vui lòng thử lại"),
	FOLDER_DELETE_FAILED(
			1513,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Xoá thư mục thất bại, vui lòng thử lại"),
	FILE_NOT_BLANK(
			1514,
			HttpStatus.NOT_FOUND,
			"Tệp không được để trống"),
	INVALID_FILE_PATH(
			1515,
			HttpStatus.BAD_REQUEST,
			"Đường dẫn tệp không hợp lệ, cú pháp đúng: folderName/fileName.ext"),
	EMPTY_SOURCE_KEY(
			1516,
			HttpStatus.BAD_REQUEST,
			"Đường dẫn tệp nguồn (sourceKey) không được để trống"),
	EMPTY_SOURCE_LIST(
			1517,
			HttpStatus.BAD_REQUEST,
			"Danh sách tệp nguồn (sourceKeys) không được để trống"),
	DESTINATION_FOLDER_EMPTY(
			1518,
			HttpStatus.BAD_REQUEST,
			"Tên thư mục đích (destinationFolder) không được để trống"),

	// Module Hub errors
	HUB_NOT_FOUND(1601, HttpStatus.NOT_FOUND, "Không tìm thấy hub"), HUB_CODE_EXISTED(1602, HttpStatus.CONFLICT,
			"Mã hub đã tồn tại trong hệ thống"),
	HUB_CODE_NOT_BLANK(1603, HttpStatus.BAD_REQUEST,
			"Mã hub không được để trống"),
	HUB_NAME_NOT_BLANK(1604, HttpStatus.BAD_REQUEST,
			"Tên hub không được để trống"),

	// Module Category errors
	CATEGORY_NOT_FOUND(1701, HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"), CATEGORY_SLUG_EXISTED(1702,
			HttpStatus.CONFLICT, "Slug danh mục đã tồn tại"),
	CATEGORY_NAME_NOT_BLANK(1703, HttpStatus.BAD_REQUEST,
			"Tên danh mục không được để trống"),
	CATEGORY_SLUG_NOT_BLANK(1704, HttpStatus.BAD_REQUEST,
			"Slug danh mục không được để trống"),
	CATEGORY_HAS_PRODUCTS(1705, HttpStatus.BAD_REQUEST,
			"Không thể xóa danh mục vì còn sản phẩm liên kết"),
	CATEGORY_HAS_CHILDREN(1706,
			HttpStatus.BAD_REQUEST,
			"Không thể xóa danh mục vì còn danh mục con"),
	CATEGORY_PARENT_NOT_FOUND(
			1707, HttpStatus.NOT_FOUND,
			"Không tìm thấy danh mục cha"),
	CATEGORY_CIRCULAR_REFERENCE(1708,
			HttpStatus.BAD_REQUEST,
			"Không thể thiết lập danh mục cha tạo thành vòng lặp"),

	// Module Product errors
	PRODUCT_NOT_FOUND(1801, HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"), PRODUCT_SLUG_EXISTED(1802,
			HttpStatus.CONFLICT, "Slug sản phẩm đã tồn tại"),
	PRODUCT_NAME_NOT_BLANK(1803, HttpStatus.BAD_REQUEST,
			"Tên sản phẩm không được để trống"),
	PRODUCT_SLUG_NOT_BLANK(1804, HttpStatus.BAD_REQUEST,
			"Slug sản phẩm không được để trống"),
	PRODUCT_DAILY_PRICE_REQUIRED(1805,
			HttpStatus.BAD_REQUEST,
			"Giá thuê theo ngày không được để trống"),
	PRODUCT_DAILY_PRICE_MIN(1806,
			HttpStatus.BAD_REQUEST,
			"Giá thuê theo ngày phải lớn hơn 0"),
	PRODUCT_DEPOSIT_REQUIRED(1807,
			HttpStatus.BAD_REQUEST,
			"Số tiền đặt cọc không được để trống"),
	PRODUCT_DEPOSIT_MIN(1808,
			HttpStatus.BAD_REQUEST,
			"Số tiền đặt cọc phải lớn hơn hoặc bằng 0"),
	PRODUCT_IMAGE_NOT_FOUND(
			1809, HttpStatus.NOT_FOUND,
			"Không tìm thấy hình ảnh sản phẩm"),

	// Module Inventory errors
	INVENTORY_ITEM_NOT_FOUND(1901, HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị"), INVENTORY_SERIAL_EXISTED(1902,
			HttpStatus.CONFLICT, "Số serial đã tồn tại trong hệ thống"),
	INVENTORY_SERIAL_NOT_BLANK(1903,
			HttpStatus.BAD_REQUEST, "Số serial không được để trống"),
	INVENTORY_NOT_AVAILABLE(1904,
			HttpStatus.BAD_REQUEST, "Thiết bị không có sẵn để cho thuê"),
	INVENTORY_NOT_FOUND(1905,
			HttpStatus.NOT_FOUND,
			"Không tìm thấy thiết bị trong kho"),
	INVENTORY_INSUFFICIENT_STOCK(1906,
			HttpStatus.BAD_REQUEST, "Không đủ số lượng tồn kho cho sản phẩm này"),

	// Module Cart errors
	CART_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng"), CART_LINE_NOT_FOUND(2002,
			HttpStatus.NOT_FOUND, "Không tìm thấy mục trong giỏ hàng"),
	CART_EMPTY(2003, HttpStatus.BAD_REQUEST,
			"Giỏ hàng trống"),
	CART_RENTAL_DATE_INVALID(2004, HttpStatus.BAD_REQUEST,
			"Ngày thuê không hợp lệ, ngày bắt đầu phải trước ngày kết thúc"),
	CART_RENTAL_MIN_DAYS(2005,
			HttpStatus.BAD_REQUEST,
			"Số ngày thuê không đạt tối thiểu yêu cầu của sản phẩm"),
	CART_QUANTITY_MIN_1(2006,
			HttpStatus.BAD_REQUEST, "Số lượng phải lớn hơn hoặc bằng 1"),
	RENTAL_DURATION_DAYS_REQUIRED(
			2007, HttpStatus.BAD_REQUEST,
			"Thời lượng thuê không được để trống"),
	RENTAL_DURATION_DAYS_MIN_1(2008,
			HttpStatus.BAD_REQUEST,
			"Thời lượng thuê phải lớn hơn hoặc bằng 1 ngày"),

	// Module Voucher errors
	VOUCHER_NOT_FOUND(2101, HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá"), VOUCHER_CODE_EXISTED(2102,
			HttpStatus.CONFLICT, "Mã giảm giá đã tồn tại"),
	VOUCHER_CODE_NOT_BLANK(2103, HttpStatus.BAD_REQUEST,
			"Mã giảm giá không được để trống"),
	VOUCHER_EXPIRED(2104, HttpStatus.BAD_REQUEST,
			"Mã giảm giá đã hết hạn"),
	VOUCHER_USAGE_LIMIT_REACHED(2105, HttpStatus.BAD_REQUEST,
			"Mã giảm giá đã hết lượt sử dụng"),
	VOUCHER_INACTIVE(2106, HttpStatus.BAD_REQUEST,
			"Mã giảm giá không còn hoạt động"),
	VOUCHER_MIN_RENTAL_DAYS_NOT_MET(2107,
			HttpStatus.BAD_REQUEST,
			"Số ngày thuê không đạt yêu cầu tối thiểu của mã giảm giá"),

	// Module Rental Order errors
	RENTAL_ORDER_NOT_FOUND(2201, HttpStatus.NOT_FOUND,
			"Không tìm thấy đơn thuê"),
	RENTAL_ORDER_INVALID_STATUS_TRANSITION(2202, HttpStatus.BAD_REQUEST,
			"Chuyển đổi trạng thái đơn thuê không hợp lệ"),
	RENTAL_ORDER_CANNOT_CANCEL(2203,
			HttpStatus.BAD_REQUEST,
			"Không thể hủy đơn thuê ở trạng thái hiện tại"),
	RENTAL_ORDER_RECIPIENT_NAME_NOT_BLANK(2204,
			HttpStatus.BAD_REQUEST,
			"Tên người nhận không được để trống"),
	RENTAL_ORDER_PHONE_NOT_BLANK(2205,
			HttpStatus.BAD_REQUEST,
			"Số điện thoại giao hàng không được để trống"),
	RENTAL_ORDER_START_DATE_REQUIRED(
			2206, HttpStatus.BAD_REQUEST,
			"Ngày bắt đầu thuê không được để trống"),
	RENTAL_ORDER_END_DATE_REQUIRED(
			2207, HttpStatus.BAD_REQUEST,
			"Ngày kết thúc thuê không được để trống"),
	RENTAL_ORDER_DATE_INVALID(
			2208, HttpStatus.BAD_REQUEST,
			"Ngày bắt đầu phải trước ngày kết thúc"),
	RENTAL_ORDER_LINE_NOT_FOUND(
			2209, HttpStatus.NOT_FOUND,
			"Không tìm thấy dòng đơn thuê"),
	RENTAL_ORDER_INVALID_DATE_RANGE(
			2210, HttpStatus.BAD_REQUEST,
			"Khoảng thời gian thuê không hợp lệ, ngày bắt đầu phải trước ngày kết thúc"),
	RENTAL_ORDER_MIN_DAYS_NOT_MET(
			2211,
			HttpStatus.BAD_REQUEST,
			"Số ngày thuê không đạt yêu cầu tối thiểu của sản phẩm"),
	RENTAL_ORDER_EXPECTED_DELIVERY_DATE_REQUIRED(
			2212, HttpStatus.BAD_REQUEST,
			"Ngày giao dự kiến không được để trống"),
	RENTAL_ORDER_EXTENSION_CONFLICT(
			2213, HttpStatus.CONFLICT,
			"Không thể gia hạn vì xung đột lịch đặt thiết bị"),

	// Module Payment Transaction errors
	PAYMENT_NOT_FOUND(2301, HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch thanh toán"), PAYMENT_ALREADY_PROCESSED(
			2302, HttpStatus.BAD_REQUEST, "Giao dịch đã được xử lý trước đó"),
	PAYMENT_AMOUNT_REQUIRED(2303,
			HttpStatus.BAD_REQUEST, "Số tiền thanh toán không được để trống"),
	PAYMENT_AMOUNT_MIN(2304,
			HttpStatus.BAD_REQUEST, "Số tiền thanh toán phải lớn hơn 0"),
	VNPAY_PAYMENT_FAILED(2305,
			HttpStatus.BAD_REQUEST, "Thanh toán VNPay thất bại"),

	// Module Rental Contract errors
	CONTRACT_NOT_FOUND(2401, HttpStatus.NOT_FOUND, "Không tìm thấy hợp đồng thuê"), CONTRACT_NUMBER_EXISTED(2402,
			HttpStatus.CONFLICT, "Số hợp đồng đã tồn tại"),
	CONTRACT_ALREADY_EXISTS_FOR_ORDER(2403, HttpStatus.CONFLICT,
			"Đơn thuê này đã có hợp đồng"),

	// Module Product Review errors
	REVIEW_NOT_FOUND(2501, HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"), REVIEW_ALREADY_EXISTS(2502,
			HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này cho đơn thuê này rồi"),
	REVIEW_RATING_REQUIRED(2503,
			HttpStatus.BAD_REQUEST, "Điểm đánh giá không được để trống"),
	REVIEW_RATING_RANGE(2504,
			HttpStatus.BAD_REQUEST, "Điểm đánh giá phải từ 1 đến 5"),
	REVIEW_ORDER_NOT_COMPLETED(2505,
			HttpStatus.BAD_REQUEST,
			"Chỉ có thể đánh giá khi đơn thuê đã hoàn thành"),
	REVIEW_NOT_ORDER_OWNER(2506,
			HttpStatus.FORBIDDEN, "Bạn không phải chủ sở hữu đơn thuê này để đánh giá"),

	// Module Contact Ticket errors
	TICKET_NOT_FOUND(2601, HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu hỗ trợ"), TICKET_SUBJECT_NOT_BLANK(2602,
			HttpStatus.BAD_REQUEST, "Chủ đề yêu cầu hỗ trợ không được để trống"),
	TICKET_MESSAGE_NOT_BLANK(2603,
			HttpStatus.BAD_REQUEST, "Nội dung yêu cầu hỗ trợ không được để trống"),
	TICKET_ALREADY_CLOSED(2604,
			HttpStatus.BAD_REQUEST, "Yêu cầu hỗ trợ đã được đóng"),

	// Module Policy Document errors
	POLICY_NOT_FOUND(2701, HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu chính sách"), POLICY_CODE_NOT_BLANK(2702,
			HttpStatus.BAD_REQUEST, "Mã chính sách không được để trống"),
	POLICY_TITLE_NOT_BLANK(2703,
			HttpStatus.BAD_REQUEST, "Tiêu đề chính sách không được để trống"),
	POLICY_VERSION_EXISTED(2704,
			HttpStatus.CONFLICT, "Phiên bản chính sách đã tồn tại"),

	// Module User Consent errors
	CONSENT_NOT_FOUND(2801, HttpStatus.NOT_FOUND, "Không tìm thấy bản đồng ý"), CONSENT_ALREADY_EXISTS(2802,
			HttpStatus.CONFLICT, "Người dùng đã đồng ý với chính sách này rồi"),
	CONSENT_ALREADY_GIVEN(2803,
			HttpStatus.CONFLICT, "Người dùng đã đồng ý với chính sách này trước đó"),;

	int code;
	HttpStatusCode statusCode;
	String message;
}
