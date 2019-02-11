<jsp:include page="../cdg_header.jsp" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script>
$(document).ready(function() {
	$("#feed_id").change(function() {
		document.getElementById('load_type').style.display = "inline-flex";
		document.getElementById('schdiv').innerHTML = "";
		document.getElementById('datadiv').innerHTML = "";
	});
	$("#feed_id1").change(function() {
		document.getElementById('load_type').style.display = "none";
		document.getElementById('schdiv').innerHTML = "";
		document.getElementById('datadiv').innerHTML = "";
		document.getElementById('bord').style.display = "block";
		var src_sys_id = document.getElementById("feed_id1").value;
		var src_val = document.getElementById("src_val").value;
		$.post('${pageContext.request.contextPath}/extraction/DataDetailsEditOracle',
		{
			src_sys_id : src_sys_id,
			src_val : src_val
		},
		function(data) {
			$('#datadiv').html(data)
		});
	});
	$("#success-alert").hide();
	$("#success-alert").fadeTo(10000, 10).slideUp(2000,
		function() {
		
	});
	$("#error-alert").hide();
		$("#error-alert").fadeTo(10000, 10).slideUp(2000,
		function() {
	});
});

function funccheck(val) {
	if (val == 'create') {
		//window.location.reload();
		window.location.href = "${pageContext.request.contextPath}/extraction/DataDetails";
	} else if (val == 'edit') {
		document.getElementById('feed_id').style.display = "none";
		document.getElementById('feed_id1').style.display = "block";
		document.getElementById('load_type').style.display = "none";
		document.getElementById('bord').style.display = "none";
		document.getElementById('schdiv').innerHTML = "";
		document.getElementById('datadiv').innerHTML = "";
	}
}
function loadcheck(val) {
	if (val == 'ind_load') {
		var selection = $("input[name='radio']:checked").val();
		var src_val = document.getElementById("src_val").value;
		if (selection == 'create') {
			var src_sys_id = document.getElementById("feed_id").value;
			$.post('${pageContext.request.contextPath}/extraction/DataDetailsOracle0',
			{
				src_sys_id : src_sys_id,
				src_val : src_val
			}, function(data) {
				$('#schdiv').html(data)
			});
		} else if (selection == 'edit') {
			var src_sys_id = document.getElementById("feed_id1").value;
			$.post('${pageContext.request.contextPath}/extraction/DataDetailsEditOracle',
			{
				src_sys_id : src_sys_id,
				src_val : src_val
			}, function(data) {
				$('#datadiv').html(data)
			});
		}
	} else if (val == 'bulk_load') {
		var selection = $("input[name='radio']:checked").val();
		var src_sys_id = document.getElementById("feed_id").value;
		$.post('${pageContext.request.contextPath}/extraction/BulkLoadTest',
		{
			src_sys_id : src_sys_id,
			src_val : src_val,
			selection : selection
		}, function(data) {
			$('#datadiv').html(data)
		});
	}
}

function jsonconstruct() {
	for (var y = 1; y <= document.getElementById("counter").value; y++) {
		var col="";
		var ch = document.querySelectorAll("#sel"+y+" button");
		for (var i = 0; i<ch.length; i++) {
			if(ch[i].value==='*') {
				col="*";
				break;
			}
			else {
			col=col+","+ch[i].value;
			}
		}
		if(col!="*") {
			col=col.substring(1);
		}
		document.getElementById("col_name"+y).value=col;
		document.getElementById("columns_name"+y).value=col;
		var tok="";
		var ch1 = document.querySelectorAll("#tok"+y+" button");
		for (var i = 0; i<ch1.length; i++) {
			if(ch1[i].value==='*') {
				tok="*";
				break;
			}
			else {
			tok=tok+","+ch1[i].value;
			}
		}
		if(tok!="*") {
			tok=tok.substring(1);
		}
		document.getElementById("token"+y).value=tok;
		if (document.getElementById("where_clause" + y).value === "") {
			document.getElementById("where_clause" + y).value = "1=1";
		}
	}
	
	var errors = [];
	var selection = $("input[name='radio']:checked").val();
	if (selection == 'create') {
		var feed_id = document.getElementById("feed_id").value;
		var upload_type = $("input[name='bulk']:checked").val();
		if (!checkLength(feed_id)) {
			errors[errors.length] = "Feed Name";
		}
		if (!checkLength(upload_type)) {
			errors[errors.length] = "Upload Type";
		}
		if (upload_type == "ind_load") {
			var ct = document.getElementById("counter").value;
			for (var i = 1; i <= ct; i++) {
				var schema = document.getElementById("schema_name" + i).value;
				if (!checkLength(schema)) {
					errors[errors.length] = "Schema Name " + i;
				} else {
					var tbl = document.getElementById("table_name" + i).value;
					if (!checkLength(tbl)) {
						errors[errors.length] = "Table Name " + i;
					} else {
						var col = document.getElementById("col_name" + i).value;
						if (!checkLength(col)) {
							errors[errors.length] = "Column Names " + i;
						}
					}
				}
			}
		}
		if (upload_type == "bulk_load") {
			if (document.getElementById("file").files.length == 0) {
				errors[errors.length] = "Upload File Details";
			}
		}
	} else {
		var feed_id = document.getElementById("feed_id1").value;
		if (!checkLength(feed_id)) {
			errors[errors.length] = "Feed Name";
		}
		var ct = document.getElementById("counter").value;
		for (var i = 1; i <= ct; i++) {
			var schema = document.getElementById("schema_name" + i).value;
			if (!checkLength(schema)) {
				errors[errors.length] = "Schema Name " + i;
			} else {
				var tbl = document.getElementById("table_name" + i).value;
				if (!checkLength(tbl)) {
					errors[errors.length] = "Table Name " + i;
				} else {
					var col = document.getElementById("col_name" + i).value;
					if (!checkLength(col)) {
						errors[errors.length] = "Column Names " + i;
					}
				}
			}
		}
	}
	if (errors.length > 0) {
		reportErrors(errors);
		return false;
	}

	var data = {};
	$(".form-control").serializeArray().map(function(x) {
		data[x.name] = x.value;
	});
	var x = '{"header":{},"body":{"data":' + JSON.stringify(data) + '}}';
	document.getElementById('x').value = x;
	alert(x);
	console.log(x);
	document.getElementById('DataDetails').submit();
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
							if (request.getAttribute("successString") != null) {
						%>
						<div class="alert alert-success" id="success-alert">
							<button type="button" class="close" data-dismiss="alert">x</button>
							${successString}
						</div>
						<%
							}
						%>
						<%
							if (request.getAttribute("errorString") != null) {
						%>
						<div class="alert alert-danger" id="error-alert">
							<button type="button" class="close" data-dismiss="alert">x</button>
							${errorString}
						</div>
						<%
							}
						%>
						<form class="forms-sample" id="DataDetails" name="DataDetails"
							method="POST"
							action="${pageContext.request.contextPath}/extraction/DataDetailsOracle3"
							enctype="application/json">
							<input type="hidden" name="x" id="x" value=""> <input
								type="hidden" name="src_val" id="src_val" value="${src_val}">
							<input type="hidden" name="project" id="project"
								class="form-control" value="${project}"> <input
								type="hidden" name="user" id="user" class="form-control"
								value="${usernm}">
								<input type="hidden" name="counter" id="counter" class="form-control" value="1">

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
											value="edit" onclick="funccheck(this.value)">
											Edit/View
										</label>
									</div>
								</div>
							</div>

							<div class="form-group">
								<label>Source Feed Name *</label> <select name="feed_id"
									id="feed_id" class="form-control">
									<option value="" selected disabled>Source Feed Name
										...</option>
									<c:forEach items="${src_sys_val1}" var="src_sys_val1">
										<option value="${src_sys_val1.src_sys_id}">${src_sys_val1.src_unique_name}</option>
									</c:forEach>
								</select> <select name="feed_id1" id="feed_id1" class="form-control"
									style="display: none;">
									<option value="" selected disabled>Source Feed Name
										...</option>
									<c:forEach items="${src_sys_val2}" var="src_sys_val2">
										<option value="${src_sys_val2.src_sys_id}">${src_sys_val2.src_unique_name}</option>
									</c:forEach>
								</select>
							</div>
							<div class="form-group row" id="load_type"
								style="display: none; width: 100%;">
								<label class="col-sm-3 col-form-label">Upload Type</label>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="bulk" id="bulk1"
											value="ind_load" onclick="loadcheck(this.value)">
											Individual Load Type
										</label>
									</div>
								</div>
								<div class="col-sm-4">
									<div class="form-check form-check-info">
										<label class="form-check-label"> <input type="radio"
											class="form-check-input" name="bulk" id="bulk2"
											value="bulk_load" onclick="loadcheck(this.value)">
											Bulk Load Type
										</label>
									</div>
								</div>
							</div>
							<fieldset id="bord" class="fs" style="display:none;">
							<div id="schdiv"></div>
							<div id="datadiv"></div>
							</fieldset>
	<div id="but" style="display:none;">
	<div class="form-group" style="float: right; margin: 5px;">
		<button id="add" type="button"
			class="btn btn-rounded btn-gradient-info mr-2" onclick="return dup_div();">+</button>
	</div>
	<button onclick="return jsonconstruct();"
		class="btn btn-rounded btn-gradient-info mr-2">Save</button>
	</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		<jsp:include page="../cdg_footer.jsp" />