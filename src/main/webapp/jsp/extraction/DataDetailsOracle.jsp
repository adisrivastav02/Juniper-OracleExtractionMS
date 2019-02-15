<jsp:include page="../cdg_header.jsp" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script>
$(document).ready(function() {
	$("#feed_id").change(function() {
		document.getElementById('load_type').style.display="inline-flex";
	});
	$("#success-alert").hide();
    $("#success-alert").fadeTo(10000,10).slideUp(2000, function(){
    });   
	$("#error-alert").hide();
    $("#error-alert").fadeTo(10000,10).slideUp(2000, function(){
     });
});
	function jsonconstruct() {
		var schema = document.getElementById("schema_name").value;
		for (var y = 1; y <= document.getElementById("counter").value; y++) {
			multisel1('col_name' + y, 'columns_name' + y);
			if (document.getElementById("where_clause" + y).value === "") {
				document.getElementById("where_clause" + y).value = "1=1";
			}
		}
		var data = {};
		$(".form-control").serializeArray().map(function(x) {
			data[x.name] = x.value;
		});
		var x = '{"header":{},"body":{"data":'
				+ JSON.stringify(data) + '}}';
		document.getElementById('x').value = x;
		//alert(x);
		//console.log(x);
		document.getElementById('DataDetails').submit();
	}
	
function getsch(id, val) {
	var in1 = id.slice(-1);
	var in2 = id.slice(-2, -1);
	if (in2 === "e")
		;
	else {
		in1 = id.slice(-2);
	}
	var id = in1;
	var schema_name = val;
	var src_val = document.getElementById("src_val").value;
	var src_sys_id = document.getElementById("feed_id").value;
	$
			.post(
					'${pageContext.request.contextPath}/extraction/DataDetailsOracle1',
					{
						id : id,
						src_sys_id : src_sys_id,
						src_val : src_val,
						schema_name : schema_name
					}, function(data) {
						$('#datdiv' + id).html(data)
					});
}
function getcols(id, val) {
	var in1 = id.slice(-1);
	var in2 = id.slice(-2, -1);
	if (in2 === "e")
		;
	else {
		in1 = id.slice(-2);
	}
	var id = in1;
	var table_name = val;
	var src_val = document.getElementById("src_val").value;
	var connection_id = document.getElementById("connection_id").value;
	var schema_name = document.getElementById("schema_name"+id).value;
	$.post('${pageContext.request.contextPath}/extraction/DataDetailsOracle2',
	{
		id : id,
		src_val : src_val,
		table_name : table_name,
		connection_id : connection_id,
		schema_name : schema_name
	}, function(data) {
		$("#fldd"+id).html(data);
	});
}
function incr(id, val) {
	var in1 = id.slice(-1);
	var in2 = id.slice(-2, -1);
	if (in2 === "e")
		;
	else {
		in1 = id.slice(-2);
	}
	var in3 = 'incc' + in1;
	if (val == "incr") {
		document.getElementById(in3).style.display = "block";
	} else if (val == "full") {
		document.getElementById(in3).style.display = "none";
	}
}

	
	function funccheck(val) {
		if (val == 'create') {
			//window.location.reload();
			window.location.href="${pageContext.request.contextPath}/extraction/DataDetails";
		} else if(val=='edit') {
			document.getElementById('datdyn').innerHTML="";
			document.getElementById('schdyn').innerHTML="";
			
		}
	}
	function loadcheck(val) {
		if (val == 'ind_load') {
			var selection=$("input[name='radio']:checked").val();
			if(selection == 'create'){
			var src_sys_id = document.getElementById("feed_id").value;
			var src_val = document.getElementById("src_val").value;
			$.post('${pageContext.request.contextPath}/extraction/DataDetailsOracle0', {
					src_sys_id : src_sys_id,
					src_val : src_val
				}, function(data) {
					$('#schdyn').html(data)
				});
			}else if (selection == 'edit'){
				var src_sys_id = document.getElementById("feed_id").value;
				var src_val = document.getElementById("src_val").value;
				$.post('${pageContext.request.contextPath}/extraction/DataDetailsEditOracle', {
					src_sys_id : src_sys_id,
					src_val : src_val
				}, function(data) {
					$('#datdyn').html(data)
				});
			}
		} else if (val == 'bulk_load') {
			var selection=$("input[name='radio']:checked").val();
			var src_sys_id = document.getElementById("feed_id").value;
			$.post('${pageContext.request.contextPath}/extraction/BulkLoadTest', {
					src_sys_id : src_sys_id,
					src_val : src_val,
					selection : selection
				}, function(data) {
					$('#datdyn').html(data)
				});
		}
	}
</script>

<div class="main-panel">
	<div class="content-wrapper">
		<div class="row">
			<div class="col-12 grid-margin stretch-card">
				<div class="card">
					<div class="card-body">
						<h4 class="card-title">Data Extraction</h4>
						<p class="card-description">Data Details</p>
						<%
               if(request.getAttribute("successString") != null) {
               %>
            <div class="alert alert-success" id="success-alert">
               <button type="button" class="close" data-dismiss="alert">x</button>
               ${successString}
            </div>
            <%
               }
               %>
            <%
               if(request.getAttribute("errorString") != null) {
               %>
            <div class="alert alert-danger" id="error-alert">
               <button type="button" class="close" data-dismiss="alert">x</button>
               ${errorString}
            </div>
            <%
               }
               %>
						<form class="forms-sample" id="DataDetails" name="DataDetails"
							method="POST" action="${pageContext.request.contextPath}/extraction/DataDetailsOracle3"
							enctype="application/json">
							<input type="hidden" name="x" id="x" value=""> <input
								type="hidden" name="src_val" id="src_val" value="${src_val}">
								<input type="hidden" name="project"
								id="project" class="form-control" value="${project}"> <input
								type="hidden" name="user" id="user" class="form-control"
								value="${usernm}">

							<div class="form-group row">
								<label class="col-sm-3 col-form-label">Data Tables</label>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="radio" id="radio1"
											checked="checked" value="create"
											onclick="funccheck(this.value)"> Create
										</label>
									</div>
								</div>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="radio" id="radio2"
											value="edit" onclick="funccheck(this.value)"> Edit/View
										</label>
									</div>
								</div>
							</div>

							<div class="form-group row">
								<label>Source Feed Name *</label> <select name="feed_id"
									id="feed_id" class="form-control">
									<option value="" selected disabled>Source Feed Name
										...</option>
									<c:forEach items="${src_sys_val}" var="src_sys_val">
										<option value="${src_sys_val.src_sys_id}">${src_sys_val.src_unique_name}</option>
									</c:forEach>
								</select>
							</div>
							<div class="form-group row" id="load_type" style="display:none;width:100%;">
								<label class="col-sm-3 col-form-label">Load Type</label>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="bulk" id="bulk1"
											 value="ind_load"
											onclick="loadcheck(this.value)"> Individual Load Type
										</label>
									</div>
								</div>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="bulk" id="bulk2"
											value="bulk_load" onclick="loadcheck(this.value)"> Bulk Load Type
										</label>
									</div>
								</div>
							</div>
							<div id="schdyn"></div>
							<div id="datdyn"></div>
						</form>
					</div>
				</div>
			</div>
		</div>
<jsp:include page="../cdg_footer.jsp" />