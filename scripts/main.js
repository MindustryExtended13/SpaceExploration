var mod = Vars.mods.getMod('se')
var get = (pkg) => mod.loader.loadClass(pkg).newInstance()
if (Vars.mobile) get = (_) => null;

const Tables = get('se.util.Tables');
const tableTypeByClass = Tables.getTableTypeCl;
const tableType = Tables.getTableType;
const deepCopy = Tables.deepCopy;