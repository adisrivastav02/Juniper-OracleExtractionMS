<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<link href="${pageContext.request.contextPath}/assets/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/assets/js/select2.min.js"></script>
<script>
$(document).ready(function() {
$('.js-example-basic-single').select2();
$("#schema_name").change(function() {
var schema_name = $(this).val();
var src_val = document.getElementById("src_val").value;
var src_sys_id = document.getElementById("feed_id").value;
$.post('${pageContext.request.contextPath}/extraction/DataDetailsOracle1', {
	src_sys_id : src_sys_id,
	src_val : src_val,
	schema_name : schema_name
}, function(data) {
	$('#datdyn').html(data)
});
});
});
</script>
<div class="form-group">
	<label>Schema Name *</label> <select name="schema_name"
		id="schema_name" class="js-example-basic-single form-control">
		<option value="" selected disabled>Schema Name ...</option>
		<c:forEach items="${schema_name}" var="schema_name">
			<option value="${schema_name}">${schema_name}</option>
		</c:forEach>
	</select>
</div>