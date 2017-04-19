package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.WPTool;


public interface ParamsGetter
{
    Params getParams();

    interface DataInitable
    {
        void atLeastCollectionName(WObject wObject, DataAble dataAble);
    }

    class Params
    {
        private DataAble dataAble;
        private DataInitable dataInitable;

        /**
         * @param dataClass
         */
        public Params(Class<? extends DataAble> dataClass, DataInitable dataInitable)
        {
            this.set(dataClass);
            this.dataInitable = dataInitable;
        }

        public void setDataAble(DataAble dataAble)
        {
            this.dataAble = dataAble;
        }

        public Params(DataAble dataAble, DataInitable dataInitable)
        {
            this.set(dataAble);
            this.dataInitable = dataInitable;
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

        DataAble newData(WObject wObject)
        {
            DataAble dataAbleClone = dataAble.cloneData();
            dataInitable.atLeastCollectionName(wObject, dataAbleClone);
            return dataAbleClone;
        }

        public DataInitable getDataInitable()
        {
            return dataInitable;
        }

        public DataAble getDataAble()
        {
            return dataAble;
        }

        /**
         * 会触发{@linkplain DataInitable#atLeastCollectionName(WObject, DataAble)}
         *
         * @param wObject
         * @return
         */
        public String getCollectionName(WObject wObject)
        {
            DataAble dataAble = newData(wObject);
            return dataAble.getCollectionName();
        }

    }
}
