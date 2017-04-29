
function add(conn,data){
	var sql="INSERT INTO test1 (_id,name,age,sex,time)VALUES(?,?,?,?,?)";
	var args=[data._id,data.name,data.age,data.sex,data.time];
//	var ps = conn.prepareStatement(sql);
//	ps.setObject(1,data._id);
//	ps.setObject(2,data.name);
//	ps.setObject(3,data.age);
//	ps.setObject(4,data.sex);
//	ps.setObject(5,data.time);
	print(jdaoBridge);
	return jdaoBridge.sqlExecutor(sql,args);
};