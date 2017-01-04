package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.porter.core.util.WPTool;


public interface ParamsGetter
{
    Params getParams();

    public static class Params
    {
        private DataAble dataAble;

        /**
         * @param dataClass
         */
        public Params(Class<? extends DataAble> dataClass)
        {
            this.set(dataClass);
        }

        public Params(DataAble dataAble)
        {
            this.set(dataAble);
        }


        /**
         * @param dataClass
         */
        public void set(Class<? extends DataAble> dataClass)
        {
            try
            {
                DataAble dataAble = WPTool.newObject(dataClass);
                set(dataAble);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public void set(DataAble dataAble)
        {
            this.dataAble = dataAble;
        }

        /**
         * 得到集合（或表）名
         *
         * @return
         */
        public String getCollName()
        {
            return dataAble.getCollectionName();
        }

        DataAble newData()
        {
            return dataAble.cloneData();
        }

        public DataAble getDataAble()
        {
            return dataAble;
        }

    }
}
